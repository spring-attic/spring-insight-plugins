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

package com.springsource.insight.plugin.springweb.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.StringUtil;

/**
 * 
 */
public abstract class RestOperationCollectionTestSupport extends OperationCollectionAspectTestSupport {
	protected TestRestOperations		opsInstance;

	protected RestOperationCollectionTestSupport() {
		super();
	}

	@Before
	@Override
	public void setUp () {
		super.setUp();
		opsInstance = new TestRestOperations();
	}

	protected String createTestURI (String testName) {
		return "http://37.77.34.7:7365/" + getClass().getSimpleName() + "/" + testName;
	}

	protected Operation assertRestOperationResult (Object ignored) {
		return assertRestOperation();
	}

	protected Operation assertRestOperation () {
		return assertRestOperation(opsInstance);
	}

	protected Operation assertRestOperation (TestRestOperations restOps) {
		Operation	op=getLastEntered();
		assertNotNull("No operation collected", op);
		assertEquals("Mismatched type", RestOperationCollectionSupport.TYPE, op.getType());
		assertEquals("Mismatched method", restOps.getMethod(), op.get("method", String.class));
		assertEquals("Mismatched URI", restOps.getUri(), op.get(OperationFields.URI, String.class));
		assertRestLabel(op, restOps);
		return op;
	}

	protected void assertAspectMethod (TestRestOperations restOps) {
		RestOperationCollectionSupport	aspectInstance=getRestOperationAspect();
		String							aspectMethod=aspectInstance.getMethod();
		String							restMethod=restOps.getMethod();
		assertEquals("Mismatched declared aspect method", aspectMethod, restMethod);
	}

	protected String assertRestLabel (Operation op, TestRestOperations restOps) {
		RestOperationCollectionSupport	aspectInstance=getRestOperationAspect();
		String							aspectMethod=aspectInstance.getMethod();
		String							restMethod=restOps.getMethod();
		String							actual=op.getLabel();
		String							expected=RestOperationCollectionSupport.createLabel(restMethod, restOps.getUri());
		if (!aspectMethod.equalsIgnoreCase(restMethod)) {
			expected = RestIndirectOperationCollectionSupport.createIndirectLabel(aspectMethod, restMethod, expected); 
		}
		
		assertEquals("Mismatched label", expected, actual);
		return actual;
	}

	protected RestOperationCollectionSupport getRestOperationAspect() {
		return (RestOperationCollectionSupport) getAspect();
	}
	
	protected static class TestRestOperations implements RestOperations {
		private String	method, uri;

		public TestRestOperations () {
			super();
		}

		public String getMethod () {
			return method;
		}

		public String getUri () {
			return uri;
		}

		public <T> ResponseEntity<T> getForEntity(URI url, Class<T> responseType) throws RestClientException {
			return getForEntity(url.toString(), responseType, ArrayUtil.EMPTY_OBJECTS);	// delegate on purpose to check cflowbelow behavior
		}

		public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Object... uriVariables)
				throws RestClientException {
			return getForEntity(url, responseType, Collections.<String,Object>emptyMap());	// delegate on purpose to check cflowbelow behavior
		}

		public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Map<String, ?> uriVariables)
				throws RestClientException {
			T	result=getForObject(url, responseType, uriVariables);	// delegate on purpose to check cflowbelow behavior
			return new ResponseEntity<T>(result, HttpStatus.OK); 
		}

		public <T> T getForObject(URI url, Class<T> responseType) throws RestClientException {
			return getForObject(url.toString(), responseType, ArrayUtil.EMPTY_OBJECTS);	// delegate on purpose to check cflowbelow behavior
		}

		public <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) throws RestClientException {
			return getForObject(url, responseType, Collections.<String,Object>emptyMap());	// delegate on purpose to check cflowbelow behavior
		}

		public <T> T getForObject(String url, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
			setState(HttpMethod.GET, url);
			return Mockito.mock(responseType);
		}

		public HttpHeaders headForHeaders(URI url) throws RestClientException {
			return headForHeaders(url.toString(), ArrayUtil.EMPTY_OBJECTS);	// delegate on purpose to check cflowbelow behavior
		}

		public HttpHeaders headForHeaders(String url, Object... uriVariables) throws RestClientException {
			return headForHeaders(url, Collections.<String,Object>emptyMap());	// delegate on purpose to check cflowbelow behavior
		}

		public HttpHeaders headForHeaders(String url, Map<String, ?> uriVariables) throws RestClientException {
			setState(HttpMethod.HEAD, url);
			return new HttpHeaders();
		}

		public URI postForLocation(URI url, Object request) throws RestClientException {
			return postForLocation(url.toString(), request, ArrayUtil.EMPTY_OBJECTS);	// delegate on purpose to check cflowbelow behavior
		}

		public URI postForLocation(String url, Object request, Object... uriVariables) throws RestClientException {
			return postForLocation(url, request, Collections.<String,Object>emptyMap());	// delegate on purpose to check cflowbelow behavior
		}

		public URI postForLocation(String url, Object request, Map<String, ?> uriVariables) throws RestClientException {
			setState(HttpMethod.POST, url);
			try {
				return new URI(url);
			} catch(URISyntaxException e) {
				throw new RestClientException("Failed to convert " + url + ": " + e.getMessage(), e);
			}
		}

		public <T> ResponseEntity<T> postForEntity(URI url, Object request, Class<T> responseType) throws RestClientException {
			return postForEntity(url.toString(), request, responseType, ArrayUtil.EMPTY_OBJECTS);	// delegate on purpose to check cflowbelow behavior);
		}

		public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Object... uriVariables) throws RestClientException {
			return postForEntity(url, request, responseType, Collections.<String,Object>emptyMap());	// delegate on purpose to check cflowbelow behavior);
		}

		public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
			T result=postForObject(url, request, responseType, uriVariables);
			return new ResponseEntity<T>(result, HttpStatus.OK);
		}

		public <T> T postForObject(URI url, Object request, Class<T> responseType) throws RestClientException {
			return postForObject(url.toString(), request, responseType, ArrayUtil.EMPTY_OBJECTS);	// delegate on purpose to check cflowbelow behavior
		}
		
		public <T> T postForObject(String url, Object request, Class<T> responseType, Object... uriVariables) throws RestClientException {
			return postForObject(url, request, responseType, Collections.<String,Object>emptyMap());	// delegate on purpose to check cflowbelow behavior
		}

		public <T> T postForObject(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
			setState(HttpMethod.POST, url);
			return Mockito.mock(responseType);
		}

		public void put(URI url, Object request) throws RestClientException {
			put(url.toString(), request, ArrayUtil.EMPTY_OBJECTS);	// delegate on purpose to check cflowbelow behavior
		}

		public void put(String url, Object request, Object... uriVariables) throws RestClientException {
			put(url, request, Collections.<String,Object>emptyMap());	// delegate on purpose to check cflowbelow behavior
		}

		public void put(String url, Object request, Map<String, ?> uriVariables)
				throws RestClientException {
			setState(HttpMethod.PUT, url);
		}

		public void delete(URI url) throws RestClientException {
			delete(url.toString(), ArrayUtil.EMPTY_OBJECTS);	// delegate on purpose to check cflowbelow behavior
		}

		public void delete(String url, Object... uriVariables) throws RestClientException {
			delete(url, Collections.<String,Object>emptyMap());	// delegate on purpose to check cflowbelow behavior
		}

		public void delete(String url, Map<String, ?> uriVariables)
				throws RestClientException {
			setState(HttpMethod.DELETE, url);
		}

		public Set<HttpMethod> optionsForAllow(URI url) throws RestClientException {
			return optionsForAllow(url.toString(), ArrayUtil.EMPTY_OBJECTS);	// delegate on purpose to check cflowbelow behavior
		}

		public Set<HttpMethod> optionsForAllow(String url, Object... uriVariables) throws RestClientException {
			return optionsForAllow(url, Collections.<String,Object>emptyMap());	// delegate on purpose to check cflowbelow behavior
		}

		public Set<HttpMethod> optionsForAllow(String url, Map<String, ?> uriVariables) throws RestClientException {
			setState(HttpMethod.OPTIONS, url);
			return EnumSet.allOf(HttpMethod.class);
		}

		public <T> ResponseEntity<T> exchange(URI url, HttpMethod httpMethod, HttpEntity<?> requestEntity, Class<T> responseType)
				throws RestClientException {
			// delegate on purpose to check cflowbelow behavior
			return exchange(url.toString(), httpMethod, requestEntity, responseType, ArrayUtil.EMPTY_OBJECTS);
		}

		public <T> ResponseEntity<T> exchange(String url, HttpMethod httpMethod, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables)
				throws RestClientException {
			// delegate on purpose to check cflowbelow behavior
			return exchange(url.toString(), httpMethod, requestEntity, responseType, Collections.<String,Object>emptyMap());
		}

		public <T> ResponseEntity<T> exchange(String url, HttpMethod httpMethod, HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables)
				throws RestClientException {
			setState(httpMethod, url);
			return new ResponseEntity<T>(HttpStatus.NOT_FOUND);
		}

		public <T> T execute(URI url, HttpMethod httpMethod, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor)
				throws RestClientException {
			// delegate on purpose to check cflowbelow behavior
			return execute(url.toString(), httpMethod, requestCallback, responseExtractor, ArrayUtil.EMPTY_OBJECTS);
		}

		public <T> T execute(String url, HttpMethod httpMethod, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor, Object... uriVariables)
				throws RestClientException {
			// delegate on purpose to check cflowbelow behavior
			return execute(url.toString(), httpMethod, requestCallback, responseExtractor, Collections.<String,Object>emptyMap());
		}

		public <T> T execute(String url, HttpMethod httpMethod, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor, Map<String, ?> uriVariables)
				throws RestClientException {
			setState(httpMethod, url);
			return null;
		}

		public <T> ResponseEntity<T> exchange(URI url, HttpMethod httpMethod, HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType) throws RestClientException {
			// delegate on purpose to check cflowbelow behavior
			return exchange(url.toString(), httpMethod, requestEntity, responseType, ArrayUtil.EMPTY_OBJECTS);
		}

		public <T> ResponseEntity<T> exchange(String url, HttpMethod httpMethod, HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType, Object... uriVariables)
				throws RestClientException {
			// delegate on purpose to check cflowbelow behavior
			return exchange(url, httpMethod, requestEntity, responseType, Collections.<String,Object>emptyMap());
		}

		public <T> ResponseEntity<T> exchange(String url, HttpMethod httpMethod, HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType, Map<String, ?> uriVariables)
				throws RestClientException {
			setState(httpMethod, url);
			return null;
		}

		private void setState (HttpMethod accessType, String location) {
			setState(accessType.name(), location);
		}

		private void setState (String accessType, String location) {
			assertFalse("No access type specified", StringUtil.isEmpty(accessType));
			assertNull(accessType + ": Multiple access types: " + method, method);
			method = accessType;

			assertFalse("No location specified", StringUtil.isEmpty(location));
			assertNull(location + ": Multiple locations: " + uri, uri);
			uri = location;
		}

		public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity,
				Class<T> responseType) throws RestClientException {
				return null;
		}

		public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity,
				ParameterizedTypeReference<T> responseType) {
			return null;
		}
	}
}
