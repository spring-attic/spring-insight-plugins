/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.plugin.apache.http.hc3;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;

import com.springsource.insight.collection.ObscuredValueSetMarker;
import com.springsource.insight.collection.http.HttpObfuscator;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationUtils;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.MapUtil;
import com.springsource.insight.util.StringFormatterUtils;
import com.springsource.insight.util.StringUtil;

/**
 * Tests {@link HttpClientExecutionCollectionAspect} using embedded
 * Jetty server
 */
public class HttpClientExecutionCollectionAspectTest extends OperationCollectionAspectTestSupport {
    private static final int    TEST_PORT=7365;
    private static final String TEST_URI="http://localhost:" + TEST_PORT + "/";
    private static Server    SERVER;
    private HttpObfuscator	originalObfuscator;
    private final ObscuredValueSetMarker	marker=new ObscuredValueSetMarker();

    public HttpClientExecutionCollectionAspectTest() {
        super();
    }

    @BeforeClass
    public static void startEmbeddedServer () throws Exception {
        SERVER = new Server(TEST_PORT);
        SERVER.setHandler(new TestHandler());
        System.out.println("Starting embedded server on port " + TEST_PORT);
        SERVER.start();
        System.out.println("Started embedded server on port " + TEST_PORT);
    }

    @AfterClass
    public static void stopEmbeddedServer () throws Exception {
        if (SERVER != null) {
            System.out.println("Stopping embedded server");
            SERVER.stop();
            System.out.println("Server stopped");
        }
    }

    @Before
    @Override
    public void setUp() {
    	super.setUp();
    	
    	HttpClientExecutionCollectionAspect	aspectInstance=getAspect();
    	originalObfuscator = aspectInstance.getHttpHeadersObfuscator();
    	marker.clear();
    	aspectInstance.setHttpHeadersObfuscator(new HttpObfuscator(marker));
    }

    @After
    @Override
    public void restore() {
    	HttpClientExecutionCollectionAspect	aspectInstance=getAspect();
    	aspectInstance.setHttpHeadersObfuscator(originalObfuscator);
    	marker.clear();
    	super.restore();
    }

    @Test
    public void testExecuteMethodOnly () throws IOException {
        runExecuteMethodTest("testExecuteMethodOnly", null, null);
    }

    @Test
    public void testExecuteMethodWithHost () throws Exception {
        runExecuteHostMethodTest("testExecuteMethodWithHost", null);
    }

    @Test
    public void testExecuteMethodWithState () throws Exception {
        runExecuteHostMethodTest("testExecuteMethodWithState", new HttpState());
    }

    @Test
    public void testDefaultObfuscatedHeaders () throws Exception {
        runHeadersObfuscationTest("testDefaultObfuscatedHeaders", HttpObfuscator.DEFAULT_OBFUSCATED_HEADERS_LIST, true);
    }

    @Test
    public void testNonDefaultObfuscatedHeaders () throws Exception {
        runHeadersObfuscationTest("testNonDefaultObfuscatedHeaders", Arrays.asList("X-Hdr1", "X-Hdr2", "X-Hdr3"), false);
    }

    /**
     * This test is designed to ensure that when {@link HttpClient}'s
     * <code>execute</code> methods are called with a <code>null</code>
     * {@link HttpMethod} argument the aspect uses the {@link HttpPlaceholderMethod}
     * values instead
     */
    @Test
    public void testMissingHttpMethodArgument () throws HttpException, IOException {
        HttpClient  httpClient=new HttpClient();
        for (MissingMethodTestRunner runner : MissingMethodTestRunner.values()) {
            String  excValue=null;
            try {
                int statusCode=runner.executeMethod(httpClient);
                fail("Unexpected success for " + runner + ": " + statusCode);
            } catch(IllegalArgumentException e) {
                // expected - thrown by HttpClient when null method supplied
                excValue = StringFormatterUtils.formatStackTrace(e);
            }

            Operation   op=assertExecutionResult(HttpClientDefinitions.PLACEHOLDER_URI_VALUE,
                                                 HttpPlaceholderMethod.PLACEHOLDER,
                                                 HttpClientDefinitions.FAILED_CALL_STATUS_CODE,
                                                 true);
            assertEquals("Mismatched exception value for " + runner,
                                excValue,
                                op.get(OperationFields.EXCEPTION, String.class));
            Mockito.reset(spiedOperationCollector); // prepare for next iteration
        }
    }

    static enum MissingMethodTestRunner {
        METHODONLY {
                @Override
                public int executeMethod (HttpClient httpClient) throws HttpException, IOException {
                    return httpClient.executeMethod(null);
                }
            },
        HOSTANDMETHOD {
                @Override
                public int executeMethod (HttpClient httpClient)throws HttpException, IOException {
                    return httpClient.executeMethod(new HostConfiguration(), null);
                }
            },
        HOSTMETHODSTATE {
                @Override
                public int executeMethod (HttpClient httpClient)throws HttpException, IOException {
                    return httpClient.executeMethod(new HostConfiguration(), null, new HttpState());
                }
            };

        public abstract int executeMethod (HttpClient httpClient) throws HttpException, IOException; 
    }

    private Map<String,String> runHeadersObfuscationTest (String testName, Collection<String> headerSet, boolean defaultHeaders) throws IOException {
        HttpClient  httpClient=new HttpClient();
        String      uri=createTestUri(testName); 
        HttpMethod  method=new GetMethod(uri);
        for (String name : headerSet) {
			if ("WWW-Authenticate".equalsIgnoreCase(name)) {
				continue;	// this is a response header
			}
            method.addRequestHeader(name, name);
        }

    	HttpClientExecutionCollectionAspect	aspectInstance=getAspect();
    	HttpObfuscator				obfuscator=aspectInstance.getHttpHeadersObfuscator();
    	if (!defaultHeaders) {
    		for (String name : HttpObfuscator.DEFAULT_OBFUSCATED_HEADERS_LIST) {
    			if ("WWW-Authenticate".equalsIgnoreCase(name)) {
    				continue;	// this is a response header
    			}
                method.addRequestHeader(name, name);
    		}
    		obfuscator.incrementalUpdate(HttpObfuscator.OBFUSCATED_HEADERS_SETTING, StringUtil.implode(headerSet, ","));
    	}

        int                 response=httpClient.executeMethod(method);
        Operation           op=assertExecutionResult(uri, method, response, false);
        OperationMap        reqDetails=op.get("request", OperationMap.class);
        OperationList       reqHeaders=reqDetails.get("headers", OperationList.class);
        Map<String,String>  requestHeaders=toHeadersMap(reqHeaders);
        OperationMap        rspDetails=op.get("response", OperationMap.class);
        OperationList       rspHeaders=rspDetails.get("headers", OperationList.class);
        Map<String,String>  responseHeaders=toHeadersMap(rspHeaders);
        Map<String,String>	hdrsMap=new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        if (MapUtil.size(requestHeaders) > 0) {
        	hdrsMap.putAll(requestHeaders);
        }
        if (MapUtil.size(responseHeaders) > 0) {
        	hdrsMap.putAll(responseHeaders);
        }

        Collection<?>	obscuredValues=(ObscuredValueSetMarker) obfuscator.getSensitiveValueMarker();
        for (String name : headerSet) {
            String  value=hdrsMap.get(name);
            assertNotNull("Missing value for header: " + name, value);
            assertTrue("Unobscured value of " + name, obscuredValues.contains(value));
        }

    	if (!defaultHeaders) {
    		for (String name : HttpObfuscator.DEFAULT_OBFUSCATED_HEADERS_LIST) {
                assertFalse("Un-necessarily obscured value of " + name, obscuredValues.contains(name));
    		}
    	}

    	return hdrsMap;
    }

    private void runExecuteHostMethodTest (String testName, HttpState state) throws Exception
    {
        HttpHost            host=new HttpHost("localhost", TEST_PORT);            
        HostConfiguration   hostConfig=new HostConfiguration();
        hostConfig.setHost(host);
        runExecuteMethodTest(testName, hostConfig, state);
    }

    private void runExecuteMethodTest (String testName, HostConfiguration host, HttpState state)
            throws IOException {
        HttpClient  httpClient=new HttpClient();
        String      uri=createTestUri(testName); 
        HttpMethod  method=new GetMethod(uri);
        int         response;
        if (host == null) {
            response = httpClient.executeMethod(method);
        } else {
            response = (state == null)
                    ? httpClient.executeMethod(host, method)
                    : httpClient.executeMethod(host, method, state)
                    ;
        }
        
        handleResponse(testName, uri, method, response, true);
    }

    private Operation handleResponse (String     testName, 
                                      String     uri,
                                      HttpMethod method,
                                      int        response,
                                      boolean    checkHeaders) throws IOException {
        InputStream body=method.getResponseBodyAsStream();
        try {
            String  content=IOUtils.toString(body);
            assertEquals("Mismatched content", createResponseContent(testName), content);
        } finally {
            body.close();
        }

        return assertExecutionResult(uri, method, response, checkHeaders);
    }

    private Operation assertExecutionResult (String      uri,
                                             HttpMethod  method,
                                             int         response,
                                             boolean     checkHeaders)
    {
        return assertExecutionResult(getLastEntered(), uri, method,  response, checkHeaders);
    }

    private Operation assertExecutionResult (Operation op,
                                             String      uri,
                                             HttpMethod  method,
                                             int         response,
                                             boolean     checkHeaders) {
        assertNotNull("No operation extracted", op);
        assertEquals("Mismatched operation type for " + uri, HttpClientDefinitions.TYPE, op.getType());
        assertRequestDetails(uri, op.get("request", OperationMap.class), method, checkHeaders);
        assertResponseDetails(uri, op.get("response", OperationMap.class), method, response, checkHeaders);
        return op;
    }

    private void assertRequestDetails (String uri, OperationMap details, HttpMethod method, boolean checkHeaders) {
        assertEquals("Mismatched method", method.getName(), details.get("method"));
        assertEquals("Mismatched URI",
                            HttpClientExecutionCollectionAspect.getUri(method),
                            details.get(OperationFields.URI));
        assertEquals("Mismatched protocol",
                            HttpClientExecutionCollectionAspect.createVersionValue(method),
                            details.get("protocol"));
        if (checkHeaders) {
            assertHeadersContents(uri, "request", details, method, true);
        }
    }
    
    private void assertResponseDetails (String uri, OperationMap details, HttpMethod method, int statusCode, boolean checkHeaders) {
        assertEquals("Mismatched status code", statusCode, details.getInt("statusCode", (-1)));
        assertEquals("Mismatched reason phrase", method.getStatusText(), details.get("reasonPhrase", String.class));
        if (checkHeaders) {
            assertHeadersContents(uri, "response", details, method, false);
        }
    }

    private void assertHeadersContents (String uri, String type, OperationMap details,
                                        HttpMethod method, boolean useRequestHeaders) {
        OperationList headers=details.get("headers", OperationList.class);
        assertNotNull("No " + type + " headers for " + uri, headers);

        Header[]    hdrArray=useRequestHeaders ? method.getRequestHeaders() : method.getResponseHeaders(); 
        int         numHdrs=(hdrArray == null) ? 0 : hdrArray.length;
        assertEquals("Mismatched " + type + " number of headers", numHdrs, headers.size());
        if (numHdrs <= 0) {
            return;
        }

        Map<String,String>  opHdrs=toHeadersMap(headers);
        Map<String,String>  msgHdrs=toHeadersMap(hdrArray);
        assertEquals("Mismatched " + type + " headers map size", msgHdrs.size(), opHdrs.size());
        
        for (Map.Entry<String,String> hdrValue : msgHdrs.entrySet()) {
            String  name=hdrValue.getKey();
            assertEquals("Mismatched " + type + " value for " + name + " header",
                                hdrValue.getValue(), opHdrs.get(name));
        }
    }

    private Map<String,String> toHeadersMap (Header ... headers) {
        if (ArrayUtil.length(headers) <= 0) {
            return Collections.emptyMap();
        }

        Map<String,String>  hdrsMap=new TreeMap<String, String>();
        for (Header hdrValue : headers) {
            String  name=hdrValue.getName();
            String  value=hdrValue.getValue();
            hdrsMap.put(name, value);
        }

        return hdrsMap;
    }

    private Map<String,String> toHeadersMap (OperationList headers) {
        if ((headers == null) || (headers.size() <= 0)) {
            return Collections.emptyMap();
        }

        Map<String,String>  hdrsMap=new TreeMap<String, String>();
        for (int    index=0; index < headers.size(); index++) {
            OperationMap    hdrValue=headers.get(index, OperationMap.class);
            String          name=hdrValue.get(OperationUtils.NAME_KEY, String.class);
            String          value=hdrValue.get(OperationUtils.VALUE_KEY, String.class);
            hdrsMap.put(name, value);
        }

        return hdrsMap;
    }

    @Override
    public HttpClientExecutionCollectionAspect getAspect() {
        return HttpClientExecutionCollectionAspect.aspectOf();
    }

    private static final class TestHandler implements Handler {
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

        @SuppressWarnings("hiding")
        public void setServer(Server server) {
            this.server = server;
        }

        public void handle (String target, HttpServletRequest request,
                            HttpServletResponse response, int dispatch)
                        throws IOException, ServletException {
            int     namePos=target.lastIndexOf('/');
            String  testName=target.substring(namePos + 1);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/xml;charset=utf-8");
            response.addHeader("X-Test-Name", testName);
            response.addHeader("WWW-Authenticate", "allowed");

            Writer  writer=response.getWriter();
            try {
                writer.append(createResponseContent(testName));
            } finally {
                writer.close();
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

    static String createResponseContent (String testName)
    {
        return "<test name=\"" + testName + "\" />";
    }

    static String createTestUri (String testName)
    {
        return TEST_URI + testName;
    }
}
