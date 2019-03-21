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
package com.springsource.insight.plugin.apache.http.hc4;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
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
    private static final int TEST_PORT = 7365;
    private static final String TEST_URI = "http://localhost:" + TEST_PORT + "/";
    private static Server SERVER;
    private HttpObfuscator originalObfuscator;
    private final ObscuredValueSetMarker marker = new ObscuredValueSetMarker();

    public HttpClientExecutionCollectionAspectTest() {
        super();
    }

    @BeforeClass
    public static void startEmbeddedServer() throws Exception {
        SERVER = new Server(TEST_PORT);
        SERVER.setHandler(new TestHandler());
        System.out.println("Starting embedded server on port " + TEST_PORT);
        SERVER.start();
        System.out.println("Started embedded server on port " + TEST_PORT);
    }

    @AfterClass
    public static void stopEmbeddedServer() throws Exception {
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

        HttpClientExecutionCollectionAspect aspectInstance = getAspect();
        originalObfuscator = aspectInstance.getHttpHeadersObfuscator();
        marker.clear();
        aspectInstance.setHttpHeadersObfuscator(new HttpObfuscator(marker));
    }

    @After
    @Override
    public void restore() {
        HttpClientExecutionCollectionAspect aspectInstance = getAspect();
        aspectInstance.setHttpHeadersObfuscator(originalObfuscator);
        marker.clear();
        super.restore();
    }

    @Test
    public void testHttpUriRequest() throws IOException {
        runHttpUriRequestTest("testHttpUriRequest", null);
    }

    @Test
    public void testHttpUriRequestWithContext() throws IOException {
        runHttpUriRequestTest("testHttpUriRequestWithContext", new BasicHttpContext());
    }

    @Test
    public void testHttpHostUriRequest() throws Exception {
        runHttpHostRequestTest("testHttpHostUriRequest", null);
    }

    @Test
    public void testHttpHostUriRequestWithContext() throws Exception {
        runHttpHostRequestTest("testHttpHostUriRequestWithContext", new BasicHttpContext());
    }

    @Test
    public void testResponseHandler() throws IOException {
        runResponseHandlerTest("testResponseHandler", null);
    }

    @Test
    public void testResponseHandlerWithContext() throws IOException {
        runResponseHandlerTest("testResponseHandlerWithContext", new BasicHttpContext());
    }

    @Test
    public void testHostResponseHandler() throws Exception {
        runHostResponseHandlerTest("testHostResponseHandler", null);
    }

    @Test
    public void testHostResponseHandlerWithContext() throws Exception {
        runHostResponseHandlerTest("testHostResponseHandlerWithContext", new BasicHttpContext());
    }

    @Test
    public void testDefaultObfuscatedHeaders() throws Exception {
        runHeadersObfuscationTest("testDefaultObfuscatedHeaders", HttpObfuscator.DEFAULT_OBFUSCATED_HEADERS_LIST, true);
    }

    @Test
    public void testNonDefaultObfuscatedHeaders() throws Exception {
        runHeadersObfuscationTest("testNonDefaultObfuscatedHeaders", Arrays.asList("X-Hdr1", "X-Hdr2", "X-Hdr3"), false);
    }

    /**
     * This test is designed to ensure that when {@link HttpClient}'s
     * <code>execute</code> methods are called with a <code>null</code>
     * {@link HttpUriRequest} argument the aspect fails with {@link NullPointerException}
     * instead of {@link IllegalStateException} as it should have
     */
    @Test
    public void testMissingRequestArgument() throws IOException, ClientProtocolException {
        HttpClient httpClient = new DefaultHttpClient();
        for (MissingRequestTestRunner runner : MissingRequestTestRunner.values()) {
            String excValue = null;
            try {
                HttpResponse rsp = runner.execute(httpClient);
                fail("Unexpected sucess of " + runner + ": " + rsp);
            } catch (IllegalArgumentException e) {
                // expected - thrown by HttpClient when null request supplied
                excValue = StringFormatterUtils.formatStackTrace(e);
            }

            Operation op = assertExecutionResult(HttpClientDefinitions.PLACEHOLDER_URI_VALUE,
                    HttpPlaceholderRequest.PLACEHOLDER,
                    null, true);
            assertEquals("Mismatched exception value for " + runner,
                    excValue,
                    op.get(OperationFields.EXCEPTION, String.class));
            Mockito.reset(spiedOperationCollector); // prepare for next iteration
        }
    }

    static enum MissingRequestTestRunner {
        REQONLY {
            @Override
            public HttpResponse execute(HttpClient httpClient) throws IOException, ClientProtocolException {
                return httpClient.execute(null);
            }
        },
        REQANDCONTEXT {
            @Override
            public HttpResponse execute(HttpClient httpClient) throws IOException, ClientProtocolException {
                return httpClient.execute(null, Mockito.mock(HttpContext.class));
            }
        },
        HOSTANDREQ {
            @Override
            public HttpResponse execute(HttpClient httpClient) throws IOException, ClientProtocolException {
                return httpClient.execute(new HttpHost("localhost", TEST_PORT, "http"), null);
            }
        },
        HOSTREQCONTEXT {
            @Override
            public HttpResponse execute(HttpClient httpClient) throws IOException, ClientProtocolException {
                return httpClient.execute(new HttpHost("localhost", TEST_PORT, "http"), null, Mockito.mock(HttpContext.class));
            }
        };

        public abstract HttpResponse execute(HttpClient httpClient) throws IOException, ClientProtocolException;
    }

    private Map<String, String> runHeadersObfuscationTest(String testName, Collection<String> headerSet, boolean defaultHeaders) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        String uri = createTestUri(testName);
        HttpGet request = new HttpGet(uri);
        for (String name : headerSet) {
            if ("WWW-Authenticate".equalsIgnoreCase(name)) {
                continue;    // this is a response header
            }
            request.setHeader(name, name);
        }

        HttpClientExecutionCollectionAspect aspectInstance = getAspect();
        HttpObfuscator obfuscator = aspectInstance.getHttpHeadersObfuscator();
        if (!defaultHeaders) {
            for (String name : HttpObfuscator.DEFAULT_OBFUSCATED_HEADERS_LIST) {
                if ("WWW-Authenticate".equalsIgnoreCase(name)) {
                    continue;    // this is a response header
                }
                request.setHeader(name, name);
            }
            obfuscator.incrementalUpdate(HttpObfuscator.OBFUSCATED_HEADERS_SETTING, StringUtil.implode(headerSet, ","));
        }

        HttpResponse response = httpClient.execute(request);
        Operation op = assertExecutionResult(uri, request, response, false);
        OperationMap reqDetails = op.get("request", OperationMap.class);
        OperationList reqHeaders = reqDetails.get("headers", OperationList.class);
        Map<String, String> requestHeaders = toHeadersMap(reqHeaders);
        OperationMap rspDetails = op.get("response", OperationMap.class);
        OperationList rspHeaders = rspDetails.get("headers", OperationList.class);
        Map<String, String> responseHeaders = toHeadersMap(rspHeaders);
        Map<String, String> hdrsMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        if (MapUtil.size(requestHeaders) > 0) {
            hdrsMap.putAll(requestHeaders);
        }
        if (MapUtil.size(responseHeaders) > 0) {
            hdrsMap.putAll(responseHeaders);
        }

        Collection<?> obscuredValues = (ObscuredValueSetMarker) obfuscator.getSensitiveValueMarker();
        for (String name : headerSet) {
            String value = hdrsMap.get(name);
            assertNotNull("Missing header=" + name, value);
            assertTrue("Unobscured value of " + name, obscuredValues.contains(value));
        }

        if (!defaultHeaders) {
            for (String name : HttpObfuscator.DEFAULT_OBFUSCATED_HEADERS_LIST) {
                assertFalse("Un-necessarily obscured value of " + name, obscuredValues.contains(name));
            }
        }

        return hdrsMap;
    }

    private void runResponseHandlerTest(String testName, HttpContext context) throws IOException {
        runResponseHandlerTest(testName, null, context);
    }

    private void runHostResponseHandlerTest(String testName, HttpContext context) throws Exception {
        runResponseHandlerTest(testName, URIUtils.extractHost(new URI(createTestUri(testName))), context);
    }

    private void runResponseHandlerTest(String testName, HttpHost host, HttpContext context) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        String uri = createTestUri(testName);
        HttpGet request = new HttpGet(uri);
        // must be final or the anonymous class cannot reference it...
        final AtomicReference<HttpResponse> rspRef = new AtomicReference<HttpResponse>(null);
        ResponseHandler<HttpResponse> handler = new ResponseHandler<HttpResponse>() {
            public HttpResponse handleResponse(HttpResponse response)
                    throws ClientProtocolException, IOException {
                HttpResponse prevValue = rspRef.getAndSet(response);
                assertNull("Duplicate response handling", prevValue);
                return response;
            }
        };

        HttpResponse response;
        if (host == null) {
            response = (context == null)
                    ? httpClient.execute(request, handler)
                    : httpClient.execute(request, handler, context)
            ;
        } else {
            response = (context == null)
                    ? httpClient.execute(host, request, handler)
                    : httpClient.execute(host, request, handler, context)
            ;
        }

        assertSame("Mismatched reference and return value", response, rspRef.get());
        handleResponse(testName, uri, request, response, true);
    }

    private void runHttpUriRequestTest(String testName, HttpContext context) throws IOException {
        runHttpRequestTest(testName, null, context);
    }

    private void runHttpHostRequestTest(String testName, HttpContext context) throws Exception {
        runHttpRequestTest(testName, URIUtils.extractHost(new URI(createTestUri(testName))), context);
    }

    private void runHttpRequestTest(String testName, HttpHost host, HttpContext context) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        String uri = createTestUri(testName);
        HttpGet request = new HttpGet(uri);
        HttpResponse response;
        if (host == null) {
            response = (context == null)
                    ? httpClient.execute(request)
                    : httpClient.execute(request, context)
            ;
        } else {
            response = (context == null)
                    ? httpClient.execute(host, request)
                    : httpClient.execute(host, request, context)
            ;
        }

        handleResponse(testName, uri, request, response, true);
    }

    private Operation handleResponse(String testName,
                                     String uri,
                                     HttpRequest request,
                                     HttpResponse response,
                                     boolean checkHeaders) throws IOException {
        HttpEntity entity = response.getEntity();
        try {
            return assertExecutionResult(uri, request, response, checkHeaders);
        } finally {
            EntityUtils.consume(entity);
        }
    }

    private Operation assertExecutionResult(String uri,
                                            HttpRequest request,
                                            HttpResponse response,
                                            boolean checkHeaders) {
        Operation op = getLastEntered();
        assertNotNull("No operation extracted", op);
        assertEquals("Mismatched operation type for " + uri, HttpClientDefinitions.TYPE, op.getType());
        assertRequestDetails(uri, op.get("request", OperationMap.class), request, checkHeaders);
        assertResponseDetails(uri, op.get("response", OperationMap.class), response, checkHeaders);
        return op;
    }

    private OperationMap assertRequestDetails(String uri, OperationMap details, HttpRequest request, boolean checkHeaders) {
        assertNotNull("No request details for " + uri, details);

        RequestLine reqLine = request.getRequestLine();
        assertEquals("Mismatched method", reqLine.getMethod(), details.get("method"));
        assertEquals("Mismatched URI", reqLine.getUri(), details.get(OperationFields.URI));
        assertEquals("Mismatched protocol",
                HttpClientExecutionCollectionAspect.createVersionValue(reqLine.getProtocolVersion()),
                details.get("protocol"));
        if (checkHeaders) {
            assertHeadersContents(uri, "request", details, request);
        }

        return details;
    }

    private OperationMap assertResponseDetails(String uri, OperationMap details, HttpResponse response, boolean checkHeaders) {
        if (response == null) {
            return details;
        }

        StatusLine statusLine = response.getStatusLine();
        assertEquals("Mismatched status code", statusLine.getStatusCode(), details.getInt("statusCode", (-1)));
        assertEquals("Mismatched reason phrase", statusLine.getReasonPhrase(), details.get("reasonPhrase", String.class));
        if (checkHeaders) {
            assertHeadersContents(uri, "response", details, response);
        }

        return details;
    }

    private void assertHeadersContents(String uri, String type, OperationMap details, HttpMessage message) {
        OperationList headers = details.get("headers", OperationList.class);
        assertNotNull("No " + type + " headers for " + uri, headers);

        Header[] hdrArray = message.getAllHeaders();
        int numHdrs = (hdrArray == null) ? 0 : hdrArray.length;
        assertEquals("Mismatched " + type + " number of headers", numHdrs, headers.size());
        if (numHdrs <= 0) {
            return;
        }

        Map<String, String> opHdrs = toHeadersMap(headers);
        Map<String, String> msgHdrs = toHeadersMap(hdrArray);
        assertEquals("Mismatched " + type + " headers map size", msgHdrs.size(), opHdrs.size());

        for (Map.Entry<String, String> hdrValue : msgHdrs.entrySet()) {
            String name = hdrValue.getKey();
            assertEquals("Mismatched " + type + " value for " + name + " header",
                    hdrValue.getValue(), opHdrs.get(name));
        }
    }

    private Map<String, String> toHeadersMap(Header... headers) {
        if (ArrayUtil.length(headers) <= 0) {
            return Collections.emptyMap();
        }

        Map<String, String> hdrsMap = new TreeMap<String, String>();
        for (Header hdrValue : headers) {
            String name = hdrValue.getName();
            String value = hdrValue.getValue();
            hdrsMap.put(name, value);
        }

        return hdrsMap;
    }

    private Map<String, String> toHeadersMap(OperationList headers) {
        if ((headers == null) || (headers.size() <= 0)) {
            return Collections.emptyMap();
        }

        Map<String, String> hdrsMap = new TreeMap<String, String>();
        for (int index = 0; index < headers.size(); index++) {
            OperationMap hdrValue = headers.get(index, OperationMap.class);
            String name = hdrValue.get(OperationUtils.NAME_KEY, String.class);
            String value = hdrValue.get(OperationUtils.VALUE_KEY, String.class);
            hdrsMap.put(name, value);
        }

        return hdrsMap;
    }

    @Override
    public HttpClientExecutionCollectionAspect getAspect() {
        return HttpClientExecutionCollectionAspect.aspectOf();
    }

    private static final class TestHandler implements Handler {
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

        public void handle(String target, HttpServletRequest request,
                           HttpServletResponse response, int dispatch)
                throws IOException, ServletException {
            int namePos = target.lastIndexOf('/');
            String testName = target.substring(namePos + 1);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/xml;charset=utf-8");
            response.addHeader("X-Test-Name", testName);
            response.addHeader("WWW-Authenticate", "allowed");

            Writer writer = response.getWriter();
            try {
                writer.append(createResponseContent(testName));
            } finally {
                writer.close();
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

    static String createResponseContent(String testName) {
        return "<test name=\"" + testName + "\" />";
    }

    static String createTestUri(String testName) {
        return TEST_URI + testName;
    }
}
