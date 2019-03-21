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

package com.springsource.insight.plugin.jmx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.springsource.insight.intercept.operation.Operation;


/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(JmxOperationCollectionTestSupport.TEST_CONTEXT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JmxSetMultipleAttrsOperationCollectionAspectTest extends JmxMultiAttributeCollectionTestSupport {
    public JmxSetMultipleAttrsOperationCollectionAspectTest() {
        super(JmxPluginRuntimeDescriptor.SET_ACTION);
    }

    @Test
    public void testDirectSetMultipleAttributes() throws Exception {
        testSetMultipleAttributes(mbeanServer);
    }

    @Test    // make sure cflowbelow is activated
    public void testDelegatedSetMultipleAttributes() throws Exception {
        testSetMultipleAttributes(new DelegatingMBeanServer(mbeanServer));
    }

    private void testSetMultipleAttributes(MBeanServer server) throws Exception {
        ObjectName name = new ObjectName(SpringMBeanComponent.RESOURCE_NAME);
        Object[] valPairs = {
                "StringValue", getClass().getSimpleName()
                + "#testSetMultipleAttributes("
                + server.getClass().getSimpleName()
                + "@" + System.identityHashCode(server)
                + ")",
                "NumberValue", Long.valueOf(System.nanoTime())
        };
        Map<String, Object> valuesMap = new TreeMap<String, Object>();
        for (int index = 0; index < valPairs.length; index += 2) {
            valuesMap.put(String.valueOf(valPairs[index]), valPairs[index + 1]);
        }

        List<String> attrsNames = new ArrayList<String>(valuesMap.keySet());
        for (int index = 0; index < Byte.SIZE; index++) {
            Collections.shuffle(attrsNames);

            AttributeList attrs = new AttributeList(attrsNames.size());
            for (String n : attrsNames) {
                Object v = valuesMap.get(n);
                attrs.add(new Attribute(n, v));
            }

            try {
                AttributeList result = server.setAttributes(name, attrs);
                Operation op = assertAttributesListOperation(name, attrs);
                assertEncodedManagedAttributes(op, name, result);
            } finally {
                Mockito.reset(spiedOperationCollector);
            }
        }
    }

    @Override
    public JmxSetMultipleAttrsOperationCollectionAspect getAspect() {
        return JmxSetMultipleAttrsOperationCollectionAspect.aspectOf();
    }

}
