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

package com.springsource.insight.plugin.jndi;

import java.util.Collections;

import javax.naming.NamingException;

import org.junit.Test;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.StringFormatterUtils;
import com.springsource.insight.util.StringUtil;


/**
 *
 */
public class JndiBindOperationCollectionAspectTest extends JndiOperationCollectionAspectTestSupport {
    public JndiBindOperationCollectionAspectTest() {
        super(JndiPluginRuntimeDescriptor.BIND);
    }

    @Test
    public void testBind() throws Exception {
        final String NAME = "testBind";
        final Object VALUE = Long.valueOf(System.currentTimeMillis());
        JndiTestContext context = setUpContext(Collections.<String, Object>emptyMap(),
                Collections.singletonMap(NAME, Long.valueOf(System.nanoTime())));
        context.bind(NAME, VALUE);
        assertBindOperation(context, "bind", NAME, VALUE);
    }

    @Test
    public void testRebind() throws Exception {
        final String NAME = "testRebind";
        final Object VALUE = Long.valueOf(System.currentTimeMillis());
        JndiTestContext context = setUpContext(Collections.singletonMap(NAME, Long.valueOf(3777347L)),
                Collections.singletonMap(NAME, Long.valueOf(System.nanoTime())));
        context.rebind(NAME, VALUE);
        assertBindOperation(context, "rebind", NAME, VALUE);
    }

    @Test
    public void testUnbind() throws Exception {
        final String NAME = "testUnbind";
        JndiTestContext context = setUpContext(Collections.singletonMap(NAME, Long.valueOf(System.currentTimeMillis())),
                Collections.singletonMap(NAME, Long.valueOf(System.nanoTime())));
        context.unbind(NAME);
        assertBindOperation(context, "unbind", NAME, null);
    }

    @Test
    public void testIgnoredResourcesBind() throws Exception {
        runFilteredResourcesTest("testIgnoredResourcesBind",
                new ContextOperationExecutor() {
                    public Object executeContextOperation(JndiTestContext context, String name, Object value) throws Exception {
                        context.bind(name, value);
                        return null;
                    }
                });
    }

    @Test
    public void testIgnoredResourcesRebind() throws Exception {
        runFilteredResourcesTest("testIgnoredResourcesRebind",
                new ContextOperationExecutor() {
                    public Object executeContextOperation(JndiTestContext context, String name, Object value) throws Exception {
                        context.rebind(name, value);
                        return null;
                    }
                });
    }

    @Test
    public void testIgnoredResourcesUnbind() throws Exception {
        runFilteredResourcesTest("testIgnoredResourcesUnbind",
                new ContextOperationExecutor() {
                    public Object executeContextOperation(JndiTestContext context, String name, Object value) throws Exception {
                        context.unbind(name);
                        return null;
                    }
                });
    }

    @Override
    protected void runFilteredResourcesTest(String baseName, ContextOperationExecutor executor) throws Exception {
        runFilteredResourcesTest(baseName, new JndiTestContext(true), executor);
    }

    protected Operation assertBindOperation(JndiTestContext context, String action, String name, Object value) throws NamingException {
        Operation op = assertCollectedOperation(action, name);
        if (value != null) {
            String expected = StringUtil.chopTailAndEllipsify(StringUtil.safeToString(value), StringFormatterUtils.MAX_PARAM_LENGTH);
            String actual = op.get("value", String.class);
            assertEquals(action + "[" + name + "] mismatched value", expected, actual);
        }
        assertCollectedEnvironment(op, context);
        return op;
    }

    @Override
    public JndiBindOperationCollectionAspect getAspect() {
        return JndiBindOperationCollectionAspect.aspectOf();
    }

}
