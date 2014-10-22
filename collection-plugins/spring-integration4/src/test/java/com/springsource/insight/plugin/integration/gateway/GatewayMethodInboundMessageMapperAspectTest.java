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

package com.springsource.insight.plugin.integration.gateway;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;

import com.springsource.insight.collection.test.AbstractCollectionTestSupport;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.ClassUtil;
import com.springsource.insight.util.ExtraReflectionUtils;

/**
 *
 */
public class GatewayMethodInboundMessageMapperAspectTest extends AbstractCollectionTestSupport {
    private static String CLASS_NAME = "org.springframework.integration.gateway.GatewayMethodInboundMessageMapper";

    public GatewayMethodInboundMessageMapperAspectTest() {
        assertTrue("Cannot access " + CLASS_NAME, ClassUtil.isPresent(CLASS_NAME, getClass()));
    }

    @Test
    public void testHasMethodIntroduction() throws Exception {
        Object gateway = getClassInstance("testHasMethodIntroduction");
        Class<?> gtwClass = gateway.getClass();
        assertInstanceOf(gtwClass.getName() + " " + HasMethod.class.getSimpleName() + " ?", gateway, HasMethod.class);
    }

    @Test
    public void testMethodTaggingAspect() throws Exception {
        Object gateway = getClassInstance("testMethodTaggingAspect");
        Class<?> gtwClass = gateway.getClass();
        Field methodField = ExtraReflectionUtils.getAccessibleField(gtwClass, "method", Method.class);
        assertNotNull("Cannot locate method field", methodField);

        Method expected = ExtraReflectionUtils.getFieldValue(methodField, gateway, Method.class);
        assertNotNull("No gateway method value", expected);

        assertInstanceOf(gtwClass.getName() + " " + HasMethod.class.getSimpleName() + " ?", gateway, HasMethod.class);
        HasMethod methodAccess = (HasMethod) gateway;
        Method actual = methodAccess.__getInsightMethod();
        assertSame("Mismatched tagged method value", expected, actual);
    }

    private Object getClassInstance(String methodName) throws Exception {
        Class<?> myClass = getClass();
        Class<?> instanceClass = ClassUtil.loadClassByName(CLASS_NAME, myClass);
        Constructor<?> ctor = ExtraReflectionUtils.getAccessibleConstructor(instanceClass, Method.class);
        Method method = myClass.getMethod(methodName, ArrayUtil.EMPTY_CLASSES);
        return ctor.newInstance(method);
    }
}
