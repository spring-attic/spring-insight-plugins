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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.httpinvoker.HttpInvokerRequestExecutor;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.method.JoinPointBreakDown;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.TraceError;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.MapUtil;
import com.springsource.insight.util.StringFormatterUtils;
import com.springsource.insight.util.StringUtil;

/**
 * 
 */
public class HttpInvokerRequestExecutorOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	private static final HttpInvokerRequestExecutorExternalResourceAnalyzer	extresAnalyzer=
			HttpInvokerRequestExecutorExternalResourceAnalyzer.getInstance();
	private static final HttpInvokerRequestExecutorTraceErrorAnalyzer	errorsAnalyzer=
			HttpInvokerRequestExecutorTraceErrorAnalyzer.getInstance();
	private static final String	TEST_HOST="37.77.34.7";
	private static final int	TEST_PORT=7365;
	private static final String	TEST_URL="http://" + TEST_HOST + ":" + TEST_PORT;

	public HttpInvokerRequestExecutorOperationCollectionAspectTest() {
		super();
	}

	@Test
	public void testSuccessfulRemoteInvocation() throws Exception {
		TestInvoker			invoker=new TestInvoker(Long.valueOf(System.currentTimeMillis()));
		RemoteInvocation	invocation=invoker.createRemoteInvocation("testSuccessfulRemoteInvocation");
		invocation.setAttributes(Collections.singletonMap("testSuccessfulRemoteInvocation", (Serializable) Long.valueOf(System.currentTimeMillis())));

		HttpInvokerClientConfiguration	config=
				createMockConfiguration(invocation.getMethodName(), "http://hello/world", "http://here/testSuccessfulRemoteInvocation");
		RemoteInvocationResult			result=invoker.executeRequest(config, invocation);
		Operation						op=assertRemotingOperation(config, invocation, result);
		assertExternalResource(op);
	}

	@Test
	public void testFailedRemoteInvocation() throws Exception {
		TestInvoker			invoker=new TestInvoker(new UnsupportedOperationException("testFailedRemoteInvocation"));
		RemoteInvocation	invocation=invoker.createRemoteInvocation("testFailedRemoteInvocation");
		invocation.setAttributes(Collections.singletonMap("testFailedRemoteInvocation", (Serializable) Long.valueOf(System.currentTimeMillis())));

		HttpInvokerClientConfiguration	config=
				createMockConfiguration(invocation.getMethodName(), "http://goodbye/world", "http://there/testFailedRemoteInvocation");
		RemoteInvocationResult			result=invoker.executeRequest(config, invocation);
		Operation						op=assertRemotingOperation(config, invocation, result);
		assertTraceError(op, result);
	}

	@Override
	public HttpInvokerRequestExecutorOperationCollectionAspect getAspect() {
		return HttpInvokerRequestExecutorOperationCollectionAspect.aspectOf();
	}

	private static List<TraceError> assertTraceError (Operation op, RemoteInvocationResult result) {
		List<TraceError>	errors=errorsAnalyzer.locateErrors(creatMockOperationTraceWrapper(op));
		assertEquals("Mismatched number of errors", 1, ListUtil.size(errors));

		TraceError	err=errors.get(0);
		Throwable	exc=result.getException();
		assertEquals("Mismatched error text", StringFormatterUtils.formatStackTrace(exc), err.getMessage());
		return errors;
	}

	private static ExternalResourceDescriptor assertExternalResource (Operation op) {
		Frame						frame=createMockOperationWrapperFrame(op);        
        ExternalResourceDescriptor	desc=extresAnalyzer.extractExternalResourceDescriptor(frame);
        assertNotNull("No resource", desc);
        assertSame("Mismatched external resource frame", frame, desc.getFrame());
        assertEquals("Mismatched external resource host", TEST_HOST, desc.getHost());
        assertEquals("Mismatched external resource port", TEST_PORT, desc.getPort());
        assertEquals("Mismatched external resource name", MD5NameGenerator.getName(TEST_URL), desc.getName());
        assertEquals("Mismatched external resource label", op.get(OperationFields.URI, String.class), desc.getLabel());
        assertEquals("Mismatched external resource type", ExternalResourceType.WEB_SERVER.name(), desc.getType());
        assertFalse("Unexpected as parent external resource", desc.isParent());
        assertFalse("Not outgoing external resource", desc.isIncoming());
        
        return desc;
	}

	private Operation assertRemotingOperation (HttpInvokerClientConfiguration	config,
											   RemoteInvocation					invocation,
											   RemoteInvocationResult			result) {
		Operation	op=getLastEntered();
		assertNotNull("No operation", op);
		assertEquals("Mismatched type", HttpInvokerRequestExecutorExternalResourceAnalyzer.HTTP_INVOKER, op.getType());
		assertEquals("Mismatched label", HttpInvokerRequestExecutorOperationCollectionAspect.createLabel(invocation), op.getLabel());

		assertEquals("Mismatched URI", config.getServiceUrl(), op.get(OperationFields.URI, String.class));
		assertCodebaseUrls(op.get("codebaseUrls", OperationList.class), config.getCodebaseUrl());
		assertRemoteInvocation(op, invocation);
		assertRemoteResult(op, result);
		return op;
	}

	private static Operation assertRemoteResult (Operation op, RemoteInvocationResult result) {
		Throwable	remoteError=result.getException();
		if (remoteError != null) {
			assertEquals("Mismatched remote error",
						 StringFormatterUtils.formatStackTrace(remoteError),
						 op.get(HttpInvokerRequestExecutorOperationCollector.REMOTE_EXCEPTION, String.class));
		} else {
			assertEquals("Mismatched remote value",
						 StringFormatterUtils.formatObject(result.getValue()),
						 op.get(OperationFields.RETURN_VALUE, String.class));
		}
		
		return op;
	}

	private static Operation assertRemoteInvocation (Operation op, RemoteInvocation invocation) {
		assertEquals("Mismatched full class name", RemoteInvocation.class.getName(), op.get(OperationFields.CLASS_NAME, String.class));
		assertEquals("Mismatched short class name", RemoteInvocation.class.getSimpleName(), op.get(OperationFields.SHORT_CLASS_NAME, String.class));
		assertEquals("Mismatched method name", invocation.getMethodName(), op.get(OperationFields.METHOD_NAME, String.class));
		assertEquals("Mismatched method signature",
					 JoinPointBreakDown.getMethodStringFromArgs(invocation.getMethodName(), invocation.getParameterTypes()),
					 op.get(OperationFields.METHOD_SIGNATURE, String.class));
		assertRemoteInvocationAttrs(op.get("remoteInvocationAttrs", OperationMap.class), invocation.getAttributes());
		return op;
	}

	private static OperationMap assertRemoteInvocationAttrs (OperationMap map, Map<String,?> attrs) {
		assertNotNull("No invocation attributes", map);
		assertEquals("Mismatched attributes map size", MapUtil.size(attrs), map.size());

		if (map.size() > 0) {
			for (Map.Entry<String,?> ae : attrs.entrySet()) {
				String	key=ae.getKey();
				Object	expected=ae.getValue(), actual=map.get(key, expected.getClass());
				assertEquals("Mismatched value for attribute=" + key, expected, actual);
			}
		}

		return map;
	}

	private static OperationList assertCodebaseUrls (OperationList list, String codebaseUrls) {
		assertNotNull("No encoded codebase URLs list", list);
		
		List<String>	urls=StringUtil.explode(codebaseUrls, " ", true, true);
		assertEquals("Mismatched encoded list size", ListUtil.size(urls), list.size());

		for (int	index=0; index < list.size(); index++) {
			String	expected=urls.get(index), actual=list.get(index, String.class);
			assertEquals("Mismatched codebase URI value at index " + index, expected, actual);
		}

		return list;
	}

	static HttpInvokerClientConfiguration createMockConfiguration (String path, String ... codebaseUrls) {
		return createMockConfiguration(path, (ArrayUtil.length(codebaseUrls) <= 0) ? Collections.<String>emptyList() : Arrays.asList(codebaseUrls));
	}

	static HttpInvokerClientConfiguration createMockConfiguration (String path, Collection<String> codebaseUrls) {
		HttpInvokerClientConfiguration	config=Mockito.mock(HttpInvokerClientConfiguration.class);
		Mockito.when(config.getServiceUrl()).thenReturn(TEST_URL + "/" + path);
		Mockito.when(config.getCodebaseUrl()).thenReturn(StringUtil.implode(codebaseUrls, " "));
		return config;
	}

	static class TestInvoker implements HttpInvokerRequestExecutor {
		final Object	returnValue;

		TestInvoker (Object retval) {
			if ((returnValue=retval) == null) {
				throw new IllegalStateException("No return value provided");
			}
		}

		RemoteInvocation createRemoteInvocation (String methodName) {
			if (returnValue instanceof Throwable) {
				return new RemoteInvocation(methodName, new Class[] { Object.class }, new Object[] { Void.class });
			} else {
				return new RemoteInvocation(methodName, new Class[] { returnValue.getClass() }, new Object[] { returnValue });
			}
		}

		public RemoteInvocationResult executeRequest(HttpInvokerClientConfiguration config, RemoteInvocation invocation)
				throws Exception {
			if (returnValue instanceof Throwable) {
				return new RemoteInvocationResult((Throwable) returnValue);
			} else {
				return new RemoteInvocationResult(returnValue);
			}
		}		
	}
}
