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

package com.springsource.insight.plugin.springweb.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import com.springsource.insight.collection.ObscuredValueSetMarker;
import com.springsource.insight.collection.http.HttpObfuscator;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationUtils;
import com.springsource.insight.intercept.trace.TraceError;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;


/**
 * 
 */
public class ClientHttpRequestCollectionOperationAspectTest extends OperationCollectionAspectTestSupport {
    private static final ClientHttpRequestTraceErrorAnalyzer	errAnalyzer=ClientHttpRequestTraceErrorAnalyzer.getInstance();
    private HttpObfuscator originalObfuscator;
    private final ObscuredValueSetMarker	marker=new ObscuredValueSetMarker();

    public ClientHttpRequestCollectionOperationAspectTest() {
        super();
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();

        ClientHttpRequestCollectionOperationAspect	aspectInstance=getAspect();
        ClientHttpRequestOperationCollector	collector=(ClientHttpRequestOperationCollector) aspectInstance.getCollector();
        originalObfuscator = collector.getHttpObfuscator();
        marker.clear();
        collector.setHttpObfuscator(new HttpObfuscator(marker));
    }

    @After
    @Override
    public void restore() {
        ClientHttpRequestCollectionOperationAspect	aspectInstance=getAspect();
        ClientHttpRequestOperationCollector	collector=(ClientHttpRequestOperationCollector) aspectInstance.getCollector();
        collector.setHttpObfuscator(originalObfuscator);
        marker.clear();

        super.restore();
    }

    @Test
    public void testSuccessfulExecutionCollected () throws Exception {
        ClientHttpRequest	request=
                new TestClientHttpRequest(HttpMethod.GET,
                        new URI("http://somewhere:7365/testExecutionCollected"),
                        createIdentityHttpHeaders(Arrays.asList("Req-Header1", "Req-Header2")),
                        createMockClientHttpResponse(HttpStatus.OK, createIdentityHttpHeaders(Arrays.asList("Rsp-Header1", "Rsp-Header2"))));
        ClientHttpResponse	response=request.execute();
        Operation 			op=assertExecuteRequest(request);
        assertRequestDetails(op, request);
        assertResponseDetails(op, response);

        TraceError	err=assertTraceError(op, response);
        assertNull("Unexpected trace error: " + err, err);
    }

    @Test
    public void testFailedExecutionCollected () throws Exception {
        ClientHttpRequest	request=
                new TestClientHttpRequest(HttpMethod.GET,
                        new URI("http://somewhere:7365/testExecutionCollected"),
                        createIdentityHttpHeaders(Arrays.asList("Req-Header1", "req-value1", "Req-Header2", "req-value2")),
                        createMockClientHttpResponse(HttpStatus.GATEWAY_TIMEOUT, createIdentityHttpHeaders(Arrays.asList("Rsp-Header1", "Rsp-Header2"))));
        ClientHttpResponse	response=request.execute();
        Operation 			op=assertExecuteRequest(request);
        TraceError			err=assertTraceError(op, response);
        assertNotNull("No error detected", err);
    }

    @Test
    public void testDefaultHeadersObfuscation() throws Exception {
        runObfuscationTest("testDefaultHeadersObfuscation", HttpObfuscator.DEFAULT_OBFUSCATED_HEADERS_LIST, true);
    }

    @Test
    public void testNonDefaultHeadersObfuscation() throws Exception {
        runObfuscationTest("testNonDefaultHeadersObfuscation", Arrays.asList("Hdr1", "Hdr2"), false);
    }

    @Override
    public ClientHttpRequestCollectionOperationAspect getAspect() {
        return ClientHttpRequestCollectionOperationAspect.aspectOf();
    }

    private void runObfuscationTest(String testName, Collection<String> hdrs, boolean defaultHeaders) throws Exception {
        ClientHttpRequestCollectionOperationAspect	aspectInstance=getAspect();
        ClientHttpRequestOperationCollector	collector=(ClientHttpRequestOperationCollector) aspectInstance.getCollector();
        HttpObfuscator obfuscator = collector.getHttpObfuscator();
        if (!defaultHeaders) {
            obfuscator.incrementalUpdate(HttpObfuscator.OBFUSCATED_HEADERS_SETTING, StringUtil.implode(hdrs, ","));
        }

        HttpHeaders reqHdrs = createIdentityHttpHeaders(HttpObfuscator.DEFAULT_OBFUSCATED_HEADERS_LIST);
        assertNotNull("Failed to remove response header value", reqHdrs.remove("WWW-Authenticate"));
        if (!defaultHeaders) {
            addIdentityHttpHeaders(reqHdrs, hdrs);
        }

        HttpHeaders	rspHdrs=createIdentityHttpHeaders(Collections.singletonList("WWW-Authenticate"));
        ClientHttpRequest	request=
                new TestClientHttpRequest(HttpMethod.GET,
                        new URI("http://somewhere:7365/" + testName),
                        reqHdrs,
                        createMockClientHttpResponse(HttpStatus.OK, rspHdrs));
        ClientHttpResponse	response=request.execute();
        assertNotNull("No response", response);

        ObscuredValueSetMarker	obsMarker=(ObscuredValueSetMarker) obfuscator.getSensitiveValueMarker();
        for (String name : hdrs) {
            assertTrue("Value not obscured for " + name, obsMarker.remove(name));
        }

        // if obscured headers are not the defaults, make sure defaults are not obscured
        if (!defaultHeaders) {
            for (String name : HttpObfuscator.DEFAULT_OBFUSCATED_HEADERS_LIST) {
                assertFalse("Value un-necessarily obscured for " + name, obsMarker.contains(name));
            }
        }
    }

    private TraceError assertTraceError(Operation op, ClientHttpResponse rsp) throws IOException {
        int					statusCode=rsp.getRawStatusCode();
        String				reasonPhrase=rsp.getStatusText();
        List<TraceError>	errors=errAnalyzer.locateErrors(creatMockOperationTraceWrapper(op));
        if (ClientHttpRequestTraceErrorAnalyzer.httpStatusIsError(statusCode)) {
            assertEquals("Mismatched number of errors", 1, ListUtil.size(errors));

            TraceError	err=errors.get(0);
            assertEquals("Mismatched error message", ClientHttpRequestTraceErrorAnalyzer.createErrorMessage(statusCode, reasonPhrase), err.getMessage());
            return err;
        } else {
            assertEquals("Unexpected errors: " + errors, 0, ListUtil.size(errors));
            return null;
        }
    }

    private OperationMap assertRequestDetails(Operation op, ClientHttpRequest request) {
        return assertRequestDetails(op.get("request", OperationMap.class), request);
    }

    private OperationMap assertRequestDetails(OperationMap op, ClientHttpRequest request) {
        assertNotNull("No request details", op);

        URI			uri=request.getURI();
        HttpMethod	method=request.getMethod();
        assertEquals("Mismatched method", method.name(), op.get("method", String.class));
        assertEquals("Mismatched URI", uri.toString(), op.get(OperationFields.URI, String.class));
        assertMethodHeaders("request", op, request.getHeaders());
        return op;
    }

    private OperationMap assertResponseDetails(Operation op, ClientHttpResponse response) throws IOException {
        return assertResponseDetails(op.get("response", OperationMap.class), response);
    }

    private OperationMap assertResponseDetails(OperationMap op, ClientHttpResponse response) throws IOException {
        assertNotNull("No response details", op);
        assertEquals("Mismatched response code", response.getRawStatusCode(), op.get("statusCode", Number.class).intValue());
        assertEquals("Mismatched response text", response.getStatusText(), op.get("reasonPhrase", String.class));
        assertMethodHeaders("response", op, response.getHeaders());
        return op;
    }

    private OperationList assertMethodHeaders(String type, OperationMap op, HttpHeaders hdrs) {
        return assertMethodHeaders(type, op.get("headers", OperationList.class), hdrs);
    }

    private OperationList assertMethodHeaders(String type, OperationList op, HttpHeaders hdrs) {
        assertEquals(type + ": mismatched num. of headers", op.size(), hdrs.size());

        for (int	index=0; index < op.size(); index++) {
            OperationMap	nvp=op.get(index, OperationMap.class);
            assertNotNull(type + ": missing name-value pair for index=" + index, nvp);

            String	key=nvp.get(OperationUtils.NAME_KEY, String.class);
            assertFalse(type + ": missing header name for index=" + index, StringUtil.isEmpty(key));

            String	actual=nvp.get(OperationUtils.VALUE_KEY, String.class), expected=hdrs.getFirst(key);
            assertEquals(type + ": mismatched value for header=" + key, expected, actual);
        }

        return op;
    }

    private Operation assertExecuteRequest (ClientHttpRequest request) {
        Operation	op=getLastEntered();
        assertNotNull("No operation collected", op);
        assertEquals("Mismatched type", ClientHttpRequestOperationCollector.TYPE, op.getType());

        URI			uri=request.getURI();
        HttpMethod	method=request.getMethod();
        assertEquals("Mismatched label", method + " " + uri.toString(), op.getLabel());
        return op;
    }

    @SuppressWarnings("boxing")
    private static ClientHttpResponse createMockClientHttpResponse (HttpStatus status, HttpHeaders hdrs) throws IOException {
        ClientHttpResponse	response=Mockito.mock(ClientHttpResponse.class);
        Mockito.when(response.getHeaders()).thenReturn(hdrs);
        Mockito.when(response.getStatusCode()).thenReturn(status);
        Mockito.when(response.getRawStatusCode()).thenReturn(status.value());
        Mockito.when(response.getStatusText()).thenReturn(status.getReasonPhrase());
        return response;
    }

    private static HttpHeaders createIdentityHttpHeaders (Collection<String> hdrNames) {
        return addIdentityHttpHeaders(new HttpHeaders(), hdrNames);
    }

    private static HttpHeaders addIdentityHttpHeaders (HttpHeaders hdrs, Collection<String> hdrNames) {
        if (ListUtil.size(hdrNames) <= 0) {
            return hdrs;
        }

        for (String name : hdrNames) {
            hdrs.add(name, name);
        }

        return hdrs;
    }

    static class TestClientHttpRequest implements ClientHttpRequest {
        private final ClientHttpResponse	response;
        private final HttpMethod			method;
        private final URI					uri;
        private final HttpHeaders			hdrs;

        TestClientHttpRequest (HttpMethod mthd, URI uriValue, HttpHeaders hdrsMap, ClientHttpResponse rsp) {
            if (((method=mthd) == null)
                    || ((uri=uriValue) == null)
                    || ((hdrs=hdrsMap) == null)
                    || ((response=rsp) == null)) {
                throw new IllegalStateException("Incomplete state");
            }
        }

        public HttpMethod getMethod() {
            return method;
        }

        public URI getURI() {
            return uri;
        }

        public OutputStream getBody() throws IOException {
            throw new StreamCorruptedException("No body available");
        }

        public HttpHeaders getHeaders() {
            return hdrs;
        }

        public ClientHttpResponse execute() throws IOException {
            return response;
        }
    }
}
