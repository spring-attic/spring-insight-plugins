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

package com.springsource.insight.plugin.jmx;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.method.JoinPointBreakDown;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.StringFormatterUtils;


/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(JmxOperationCollectionTestSupport.TEST_CONTEXT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JmxInvokeOperationCollectionAspectTest extends JmxOperationCollectionTestSupport {
    public JmxInvokeOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testDirectInvoke() throws Exception {
        testInvoke(mbeanServer);
    }

    @Test    // ensures cflowbelow activation
    public void testDelegatedInvoke() throws Exception {
        testInvoke(new DelegatingMBeanServer(mbeanServer));
    }

    private void testInvoke(MBeanServer server) throws Exception {
        final ObjectName name = new ObjectName(SpringMBeanComponent.RESOURCE_NAME);
        final String expMethod = "updateValues";
        final Class<?>[] methodParams = {Number.class, String.class};
        final Object[] methodArgs = {
                Long.valueOf(System.currentTimeMillis()),
                getClass().getSimpleName() + "#testInvoke("
                        + server.getClass().getSimpleName()
                        + "@" + System.identityHashCode(server)
                        + ")"
        };

        final String[] expSignature = new String[methodParams.length];
        for (int index = 0; index < methodParams.length; index++) {
            expSignature[index] = methodParams[index].getName();
        }

        server.invoke(name, expMethod, methodArgs, expSignature);
        assertInvocationOperation(name, expMethod, expSignature, methodArgs);
    }

    private Operation assertInvocationOperation(ObjectName name, String method, String[] paramsType, Object[] argVals) {
        Operation op = assertBeanOperation(name);
        assertEquals("Mismatched operation type", JmxPluginRuntimeDescriptor.INVOKE, op.getType());
        assertEquals("Mismatched operation label", JoinPointBreakDown.getMethodStringFromArgs(method, paramsType), op.getLabel());
        assertEquals("Mismatched method name", method, op.get(JmxInvocationEndPointAnalyzer.METHOD_NAME_PROP, String.class));
        assertEquals("Mismatched method signature",
                JoinPointBreakDown.createMethodParamsSignature(paramsType),
                op.get(JmxInvocationEndPointAnalyzer.SIGNATURE_NAME_PROP, String.class));
        assertInvocationOperationArguments(op, argVals);
        return op;
    }

    private OperationList assertInvocationOperationArguments(Operation op, Object... argVals) {
        return assertInvocationOperationArguments((op == null) ? null : op.get(JmxInvocationEndPointAnalyzer.INVOCATION_ARGS_PROP, OperationList.class), argVals);
    }

    private OperationList assertInvocationOperationArguments(OperationList op, Object... argVals) {
        assertNotNull("No arguments list found", op);
        assertEquals("Mismatched values count", ArrayUtil.length(argVals), op.size());
        for (int index = 0; index < op.size(); index++) {
            Object expected = StringFormatterUtils.formatObject(argVals[index]);
            Object actual = op.get(index);
            assertEquals("Mismached argument value at index=" + index, expected, actual);
        }

        return op;
    }

    @Override
    public JmxInvokeOperationCollectionAspect getAspect() {
        return JmxInvokeOperationCollectionAspect.aspectOf();
    }
}
