/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.insight.plugin.springweb.remoting;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.springframework.remoting.httpinvoker.AbstractHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.httpinvoker.SimpleHttpInvokerRequestExecutor;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.color.Color;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.trace.FrameBuilder;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.ClassUtil;
import com.springsource.insight.util.StringUtil;


/**
 * 
 */
public class SimpleHttpInvokerRequestExecutorAspectTest
		extends HttpInvokerRequestOperationCollectionTestSupport {
    private static final Log	logger=LogFactory.getLog(SimpleHttpInvokerRequestExecutorAspectTest.class);
    private static Server    SERVER;

	public SimpleHttpInvokerRequestExecutorAspectTest() {
		super();
	}

    @BeforeClass
    public static void startEmbeddedServer () throws Exception {
        SERVER = new Server(TEST_PORT);
        SERVER.setHandler(new TestHandler());
        logger.info("Starting embedded server on port " + TEST_PORT);
        SERVER.start();
        logger.info("Started embedded server on port " + TEST_PORT);
    }

    @AfterClass
    public static void stopEmbeddedServer () throws Exception {
        if (SERVER != null) {
        	logger.info("Stopping embedded server");
            SERVER.stop();
            logger.info("Server stopped");
        }
    }

	@Test
	public void testSimpleHttpInvokerRequestExecutor () throws Exception {
		RemoteInvocation	invocation=
				new RemoteInvocation("testSimpleHttpInvokerRequestExecutor", new Class[] { Long.class }, new Object[] { Long.valueOf(System.nanoTime()) });
		TestingSimpleHttpInvokerRequestExecutor	executor=new TestingSimpleHttpInvokerRequestExecutor(invocation.getMethodName());
		HttpInvokerClientConfiguration			config=createMockConfiguration(executor.getColor(), ArrayUtil.EMPTY_STRINGS);
    	RemoteInvocationResult					result=executor.executeRequest(config, invocation);
		Object									value=result.getValue();
		assertNotNull("No result value", value);
		assertTrue("Bad result value type: " + value.getClass().getSimpleName(), value instanceof RemoteInvocation);

		RemoteInvocation	resultValue=(RemoteInvocation) value;
		assertEquals("Mismatched result method", invocation.getMethodName(), resultValue.getMethodName());
		assertArrayEquals("Mismatched result signature", invocation.getParameterTypes(), resultValue.getParameterTypes());
		assertArrayEquals("Mismatched result arguments", invocation.getArguments(), resultValue.getArguments());

		Operation	op=assertRemotingOperation(config);
		assertEquals("Mismatched request method", executor.getMethod(), op.get("method", String.class));

		ExternalResourceDescriptor	desc=assertExternalResource(op);
		assertNotNull("No external resource generated", desc);
	}

	@Override
	public SimpleHttpInvokerRequestExecutorAspect getAspect() {
		return SimpleHttpInvokerRequestExecutorAspect.aspectOf();
	}

	static class TestingSimpleHttpInvokerRequestExecutor extends SimpleHttpInvokerRequestExecutor {
		private final String	color;

		TestingSimpleHttpInvokerRequestExecutor (String colorValue) {
			if (StringUtil.isEmpty(colorValue)) {
				throw new IllegalArgumentException("No color value specified");
			}
			
			color = colorValue;
			
			setBeanClassLoader(ClassUtil.getDefaultClassLoader(getClass()));
		}

		String getColor () {
			return color;
		}

		String getMethod () {
			return HTTP_METHOD_POST;
		}

		@Override
		protected void prepareConnection(HttpURLConnection connection, int contentLength) throws IOException {
			InterceptConfiguration	config=InterceptConfiguration.getInstance();
	    	FrameBuilder			builder=config.getFrameBuilder();
	        @SuppressWarnings("unchecked")
			List<Color>			colors=builder.getHint(Color.TOKEN_NAME, List.class);
	        if (colors == null) {
	        	colors = new ArrayList<Color>();
	        	builder.setHint(Color.TOKEN_NAME, colors);
	        }
	        colors.add(new Color(null, getColor(), getClass().getSimpleName(), "prepareConnection"));

			super.prepareConnection(connection, contentLength);
		}
		
	}

    static class TestHandler implements Handler {
        private Server  server;
        private boolean started;

        protected TestHandler () {
            super();
        }

        public void addLifeCycleListener(Listener listener) {
            // ignored
        }

        public void removeLifeCycleListener(Listener listener) {
            // ignored
        }

        public void stop() throws Exception {
            if (!started) {
                throw new IllegalStateException("Not started");
            }
            
            started = false;
        }
        
        public void start() throws Exception {
            if (started) {
                throw new IllegalStateException("Double start");
            }

            started = true;
        }
        
        public boolean isStopping() {
            return true;
        }
        
        public boolean isStopped() {
            return !started;
        }
        
        public boolean isStarting() {
            return true;
        }
        
        public boolean isStarted() {
            return started;
        }
        
        public boolean isRunning() {
            return started;
        }
        
        public boolean isFailed() {
            return false;
        }
        
        public Server getServer() {
            return this.server;
        }

        public void setServer(Server s) {
            this.server = s;
        }
        
        public void handle (String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
                        throws IOException, ServletException {
            ObjectInputStream	reqStream=new ObjectInputStream(request.getInputStream());
            RemoteInvocation	invocation;
            try {
            	invocation = (RemoteInvocation) reqStream.readObject();
            } catch(ClassNotFoundException e) {
            	throw new ServletException("Failed to load invocation class: " + e.getMessage(), e);
            } finally {
            	reqStream.close();
            }

            System.out.println("Invocation: " + invocation + " - args=" + Arrays.toString(invocation.getArguments()));
            assertEquals("Mismatched target value", "/" + invocation.getMethodName(), target);

            String	color=request.getHeader(Color.TOKEN_NAME);
            assertFalse("No color provided", StringUtil.isEmpty(color));

            RemoteInvocationResult	result=new RemoteInvocationResult(invocation);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(AbstractHttpInvokerRequestExecutor.CONTENT_TYPE_SERIALIZED_OBJECT);
            
            ObjectOutputStream	rspStream=new ObjectOutputStream(response.getOutputStream());
            try {
            	rspStream.writeObject(result);
            } finally {
            	rspStream.close();
            }

            Request baseRequest = (request instanceof Request)
                    ? (Request) request
                    : HttpConnection.getCurrentConnection().getRequest()
                    ;
            baseRequest.setHandled(true);
        }
        
        public void destroy() {
            if (this.server != null)
                this.server = null;
        }
    }
}
