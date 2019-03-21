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
package com.springsource.insight.plugin.socket;

import java.io.IOException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;

import com.springsource.insight.collection.ObscuredValueSetMarker;
import com.springsource.insight.collection.http.HttpObfuscator;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.MapUtil;
import com.springsource.insight.util.io.Base64;

/**
 *
 */
public class HttpURLConnectionOperationCollectionAspectTest
        extends SocketOperationCollectionAspectTestSupport {
    private static final String TEST_URI = "http://" + TEST_HOST + ":" + TEST_PORT + "/";
    private static Server SERVER;

    public HttpURLConnectionOperationCollectionAspectTest() {
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

    @Test
    public void testConnect() throws IOException {
        final String METHOD = "GET";
        HttpURLConnection conn = createConnection(METHOD, "testConnect");
        try {
            conn.connect();
            int responseCode = conn.getResponseCode();
            assertEquals("Bad response code", HttpServletResponse.SC_OK, responseCode);
        } finally {
            conn.disconnect();
        }

        Operation op = assertSocketOperation(SocketDefinitions.CONNECT_ACTION, TEST_HOST, TEST_PORT);
        assertEquals("Mismatched method", METHOD, op.get("method", String.class));

        URL url = conn.getURL();
        assertEquals("Mismatched URL", url.toExternalForm(), op.get(OperationFields.URI, String.class));

        runExternalResourceAnalyzer(op, ExternalResourceType.WEB_SERVER, TEST_HOST, TEST_PORT);
    }

    @Test
    public void testDefaultObscuredHeaders() throws IOException {
        ObscuredValueSetMarker marker =
                setupObscuredTest(HttpObfuscator.OBFUSCATED_HEADERS_SETTING, HttpObfuscator.DEFAULT_OBFUSCATED_HEADERS_VALUES);
        HttpURLConnection conn = createConnection("POST", "testDefaultObscuredHeaders");
        Map<String, List<String>> propsMap;
        try {
            propsMap = conn.getRequestProperties();
            assertTrue("No request properties map", MapUtil.size(propsMap) > 0);

            conn.connect();
            int responseCode = conn.getResponseCode();
            assertEquals("Bad response code", HttpServletResponse.SC_OK, responseCode);
        } finally {
            conn.disconnect();
        }

        for (String hdrName : HttpObfuscator.DEFAULT_OBFUSCATED_HEADERS_LIST) {
            if ("WWW-Authenticate".equals(hdrName)) {
                continue;    // this is a response header and we do not intercept them
            }

            // see https://stackoverflow.com/questions/2864062/getrequestpropertyauthorization-always-returns-null
            List<String> values = propsMap.get(hdrName);
            if (ListUtil.size(values) <= 0) {
                continue;
            }

            assertEquals("Mismatched num. of values for " + hdrName, 1, values.size());
            assertObscureTestResults(marker, hdrName, values.get(0), true);
        }
    }

    @Test
    public void testObscuredHeaders() throws IOException {
        final String hdrName = "TestObscuredHeaders", hdrValue = String.valueOf(System.nanoTime());
        ObscuredValueSetMarker marker = setupObscuredTest(HttpObfuscator.OBFUSCATED_HEADERS_SETTING, hdrName);
        HttpURLConnection conn = createConnection("POST", hdrName);
        Map<String, List<String>> propsMap;
        try {
            conn.setRequestProperty(hdrName, hdrValue);
            propsMap = conn.getRequestProperties();
            assertTrue("No request properties map", MapUtil.size(propsMap) > 0);

            conn.connect();
            int responseCode = conn.getResponseCode();
            assertEquals("Bad response code", HttpServletResponse.SC_OK, responseCode);
        } finally {
            conn.disconnect();
        }

        assertObscureTestResults(marker, hdrName, hdrValue, true);
        assertObscureTestResults(marker, hdrName, "X-Dummy-Value", false);

        for (String defName : HttpObfuscator.DEFAULT_OBFUSCATED_HEADERS_LIST) {
            // see https://stackoverflow.com/questions/2864062/getrequestpropertyauthorization-always-returns-null
            List<String> values = propsMap.get(defName);
            if (ListUtil.size(values) <= 0) {
                continue;
            }

            assertEquals("Mismatched num. of values for " + defName, 1, values.size());
            assertObscureTestResults(marker, defName, values.get(0), false);
        }
    }

    @Override
    public HttpURLConnectionOperationCollectionAspect getAspect() {
        return HttpURLConnectionOperationCollectionAspect.aspectOf();
    }

    protected HttpURLConnection createConnection(final String method, final String testName)
            throws IOException {
        URL testURL = new URL(createTestUri(testName));
        HttpURLConnection conn = (HttpURLConnection) testURL.openConnection();
        conn.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(15L));
        conn.setReadTimeout((int) TimeUnit.SECONDS.toMillis(15L));
        if ("POST".equalsIgnoreCase(method)) {
            conn.setDoOutput(true);
        }
        conn.setRequestMethod(method);

        final String authToken = Base64.encode(getClass().getSimpleName() + ":" + testName);
        conn.setRequestProperty("Authentication-Info", "Blah");
        conn.setRequestProperty("Authorization", "Basic " + authToken);
        conn.setRequestProperty("Proxy-Authenticate", "Base64 " + authToken);
        conn.setRequestProperty("Proxy-Authorization", "Basic " + authToken);
        return conn;
    }

    static String createResponseContent(String testName) {
        return "<test name=\"" + testName + "\" />";
    }

    static String createTestUri(String testName) {
        return TEST_URI + testName;
    }

    private static final class TestHandler implements Handler {
        private Server server;
        private boolean started;

        protected TestHandler() {
            super();
        }

        public void stop() throws Exception {
            if (!started) {
                throw new IllegalStateException("Not started");
            }

            started = false;
        }

        public void addLifeCycleListener(Listener listener) {
            // ignored
        }

        public void removeLifeCycleListener(Listener listener) {
            // ignored
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

        public void setServer(Server serverInstance) {
            this.server = serverInstance;
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
}
