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

package com.springsource.insight.plugin.springweb.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URI;
import java.util.Collection;

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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.color.Color;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;

/**
 *
 */
public class SimpleClientHttpRequestFactoryCollectionAspectTest extends OperationCollectionAspectTestSupport {
    private static final Log logger = LogFactory.getLog(SimpleClientHttpRequestFactoryCollectionAspectTest.class);
    private static final ClientHttpRequestExternalResourceAnalyzer extAnalyzer = ClientHttpRequestExternalResourceAnalyzer.getInstance();
    private static final int TEST_PORT = 7365;
    private static Server SERVER;

    public SimpleClientHttpRequestFactoryCollectionAspectTest() {
        super();
    }

    @BeforeClass
    public static void startEmbeddedServer() throws Exception {
        SERVER = new Server(TEST_PORT);
        SERVER.setHandler(new TestHandler());
        logger.info("Starting embedded server on port " + TEST_PORT);
        SERVER.start();
        logger.info("Started embedded server on port " + TEST_PORT);
    }

    @AfterClass
    public static void stopEmbeddedServer() throws Exception {
        if (SERVER != null) {
            logger.info("Stopping embedded server");
            SERVER.stop();
            logger.info("Server stopped");
        }
    }

    @Test
    public void testConnectionFactory() throws Exception {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setBufferRequestBody(false);
        factory.setConnectTimeout(15 * 1000);
        factory.setReadTimeout(15 * 1000);

        URI uri = new URI("http://localhost:" + TEST_PORT + "/testConnectionFactory");
        HttpMethod method = HttpMethod.GET;
        ClientHttpRequest request = factory.createRequest(uri, method);
        ClientHttpResponse response = request.execute();
        assertEquals("Mismatched response code", HttpStatus.OK.value(), response.getRawStatusCode());

        BufferedReader rdr = new BufferedReader(new InputStreamReader(response.getBody()));
        try {
            for (String line = rdr.readLine(); line != null; line = rdr.readLine()) {
                logger.info(line);
            }
        } finally {
            rdr.close();
        }

        Operation op = assertConnectionOperation(uri, method);
        assertExternalResource(op, uri);
    }

    private Operation assertConnectionOperation(URI uri, HttpMethod method) {
        return assertConnectionOperation(uri, method.name());
    }

    private Operation assertConnectionOperation(URI uri, String method) {
        return assertConnectionOperation(uri.toString(), method);
    }

    private Operation assertConnectionOperation(String uri, String method) {
        Operation op = getLastEntered();
        assertNotNull("No operation collected", op);
        assertEquals("Mismatched operation type", ClientHttpRequestExternalResourceAnalyzer.TYPE, op.getType());
        assertEquals("Mismatched label", SimpleClientHttpRequestFactoryCollectionAspect.createLabel(method, uri), op.getLabel());
        assertEquals("Mismatched URI", uri, op.get(OperationFields.URI, String.class));
        assertEquals("Mismatched method", method, op.get("method", String.class));
        return op;
    }

    private ExternalResourceDescriptor assertExternalResource(Operation op, URI uri) {
        Collection<ExternalResourceDescriptor> descs = extAnalyzer.locateExternalResourceName(creatMockOperationTraceWrapper(op));
        assertEquals("Mismatched descriptors size", 1, ListUtil.size(descs));

        ExternalResourceDescriptor desc = ListUtil.getFirstMember(descs);
        assertEquals("Mismatched host", uri.getHost(), desc.getHost());
        assertEquals("Mismatched port", uri.getPort(), desc.getPort());
        assertEquals("Mismatched type", ExternalResourceType.WEB_SERVER.name(), desc.getType());
        assertFalse("Outgoing link ?", desc.isIncoming());
        assertFalse("Parent descriptor ?", desc.isParent());

        return desc;
    }

    @Override
    public SimpleClientHttpRequestFactoryCollectionAspect getAspect() {
        return SimpleClientHttpRequestFactoryCollectionAspect.aspectOf();
    }

    static class TestHandler implements Handler {
        private Server server;
        private boolean started;

        protected TestHandler() {
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

        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
                throws IOException, ServletException {
            String color = request.getHeader(Color.TOKEN_NAME);
            assertFalse("No color provided", StringUtil.isEmpty(color));

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain");

            Writer w = response.getWriter();
            try {
                w.append("Target: ").append(target).append("\r\n");
                w.append(Color.TOKEN_NAME).append(": ").append(color).append("\r\n");
                w.append("Now: ").append(String.valueOf(System.currentTimeMillis())).append("\r\n");
            } finally {
                w.close();
            }

            Request baseRequest = (request instanceof Request)
                    ? (Request) request
                    : HttpConnection.getCurrentConnection().getRequest();
            baseRequest.setHandled(true);
        }

        public void destroy() {
            if (this.server != null)
                this.server = null;
        }
    }
}
