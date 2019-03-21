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
import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.httpinvoker.HttpInvokerRequestExecutor;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.method.JoinPointBreakDown;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.util.MapUtil;
import com.springsource.insight.util.StringFormatterUtils;

/**
 *
 */
public class HttpInvokerRequestExecutorOperationCollectionAspectTest
        extends HttpInvokerRequestOperationCollectionTestSupport {

    public HttpInvokerRequestExecutorOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testSuccessfulRemoteInvocation() throws Exception {
        TestInvoker invoker = new TestInvoker(Long.valueOf(System.currentTimeMillis()));
        RemoteInvocation invocation = invoker.createRemoteInvocation("testSuccessfulRemoteInvocation");
        invocation.setAttributes(Collections.singletonMap("testSuccessfulRemoteInvocation", (Serializable) Long.valueOf(System.currentTimeMillis())));

        HttpInvokerClientConfiguration config =
                createMockConfiguration(invocation.getMethodName(), "https://hello/world", "https://here/testSuccessfulRemoteInvocation");
        RemoteInvocationResult result = invoker.executeRequest(config, invocation);
        Operation op = assertRemotingOperation(config, invocation, result);
        ExternalResourceDescriptor desc = assertExternalResource(op);
        assertNull("Unexpected external descriptor: " + desc, desc);
    }

    @Test
    public void testFailedRemoteInvocation() throws Exception {
        TestInvoker invoker = new TestInvoker(new UnsupportedOperationException("testFailedRemoteInvocation"));
        RemoteInvocation invocation = invoker.createRemoteInvocation("testFailedRemoteInvocation");
        invocation.setAttributes(Collections.singletonMap("testFailedRemoteInvocation", (Serializable) Long.valueOf(System.currentTimeMillis())));

        HttpInvokerClientConfiguration config =
                createMockConfiguration(invocation.getMethodName(), "https://goodbye/world", "https://there/testFailedRemoteInvocation");
        RemoteInvocationResult result = invoker.executeRequest(config, invocation);
        Operation op = assertRemotingOperation(config, invocation, result);
        ExternalResourceDescriptor desc = assertExternalResource(op);
        assertNull("Unexpected external descriptor: " + desc, desc);
        assertTraceError(op, result);
    }

    protected Operation assertRemotingOperation(HttpInvokerClientConfiguration config,
                                                RemoteInvocation invocation,
                                                RemoteInvocationResult result) {
        Operation op = assertRemotingOperation(config);
        assertRemoteInvocation(op, invocation);
        assertRemoteResult(op, result);
        return op;
    }

    protected static Operation assertRemoteResult(Operation op, RemoteInvocationResult result) {
        Throwable remoteError = result.getException();
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

    protected static Operation assertRemoteInvocation(Operation op, RemoteInvocation invocation) {
        assertEquals("Mismatched full class name", TestInvoker.class.getName(), op.get(OperationFields.CLASS_NAME, String.class));
        assertEquals("Mismatched short class name", TestInvoker.class.getSimpleName(), op.get(OperationFields.SHORT_CLASS_NAME, String.class));

        String remoteLocation = JoinPointBreakDown.getMethodStringFromArgs(invocation.getMethodName(), invocation.getParameterTypes());
        assertEquals("Mismatched remote method label", remoteLocation, op.getLabel());
        assertEquals("Mismatched remote method signature", remoteLocation, op.get("remoteMethodSignature", String.class));
        assertRemoteInvocationAttrs(op.get("remoteInvocationAttrs", OperationMap.class), invocation.getAttributes());
        return op;
    }

    protected static OperationMap assertRemoteInvocationAttrs(OperationMap map, Map<String, ?> attrs) {
        assertNotNull("No invocation attributes", map);
        assertEquals("Mismatched attributes map size", MapUtil.size(attrs), map.size());

        if (map.size() > 0) {
            for (Map.Entry<String, ?> ae : attrs.entrySet()) {
                String key = ae.getKey();
                Object expected = ae.getValue(), actual = map.get(key, expected.getClass());
                assertEquals("Mismatched value for attribute=" + key, expected, actual);
            }
        }

        return map;
    }

    @Override
    public HttpInvokerRequestExecutorOperationCollectionAspect getAspect() {
        return HttpInvokerRequestExecutorOperationCollectionAspect.aspectOf();
    }

    static class TestInvoker implements HttpInvokerRequestExecutor {
        final Object returnValue;

        TestInvoker(Object retval) {
            if ((returnValue = retval) == null) {
                throw new IllegalStateException("No return value provided");
            }
        }

        RemoteInvocation createRemoteInvocation(String methodName) {
            if (returnValue instanceof Throwable) {
                return new RemoteInvocation(methodName, new Class[]{Object.class}, new Object[]{Void.class});
            } else {
                return new RemoteInvocation(methodName, new Class[]{returnValue.getClass()}, new Object[]{returnValue});
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
