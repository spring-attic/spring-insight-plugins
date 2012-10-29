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

import java.io.IOException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.URI;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

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

	public ClientHttpRequestCollectionOperationAspectTest() {
		super();
	}

	@Test
	public void testSuccessfulExecutionCollected () throws Exception {
		ClientHttpRequest	request=
				new TestClientHttpRequest(HttpMethod.GET,
					  new URI("http://somewhere:7365/testExecutionCollected"),
					  createHttpHeaders("Req-Header1", "req-value1", "Req-Header2", "req-value2"),
					  createMockClientHttpResponse(HttpStatus.OK, createHttpHeaders("Rsp-Header1", "rsp-value1", "Rso-Header2", "rsp-value2")));
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
					  createHttpHeaders("Req-Header1", "req-value1", "Req-Header2", "req-value2"),
					  createMockClientHttpResponse(HttpStatus.GATEWAY_TIMEOUT, createHttpHeaders("Rsp-Header1", "rsp-value1", "Rso-Header2", "rsp-value2")));
		ClientHttpResponse	response=request.execute();
		Operation 			op=assertExecuteRequest(request);
		TraceError			err=assertTraceError(op, response);
		assertNotNull("No error detected", err);
	}

	@Override
	public ClientHttpRequestCollectionOperationAspect getAspect() {
		return ClientHttpRequestCollectionOperationAspect.aspectOf();
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

	private static HttpHeaders createHttpHeaders (String ... pairs) {
		HttpHeaders	hdrs=new HttpHeaders();
		for (int	index=0; index < pairs.length; index += 2) {
			String	name=pairs[index], value=pairs[index+1];
			hdrs.add(name, value);
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
