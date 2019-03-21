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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.springframework.beans.factory.annotation.Autowired;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;

/**
 *
 */
public abstract class JmxOperationCollectionTestSupport extends OperationCollectionAspectTestSupport {
    public static final String TEST_CONTEXT = "classpath:META-INF/jmx-plugin-test-context.xml";
    public static final Comparator<ObjectName> BY_CANONICAL_NAME_COMPARATOR = new Comparator<ObjectName>() {
        public int compare(ObjectName o1, ObjectName o2) {
            String n1 = (o1 == null) ? null : o1.getCanonicalName();
            String n2 = (o1 == null) ? null : o2.getCanonicalName();
            return StringUtil.safeCompare(n1, n2);
        }
    };
    @Autowired
    protected MBeanServer mbeanServer;
    @Autowired
    protected SpringMBeanComponent springMBean;

    protected JmxOperationCollectionTestSupport() {
        super();
    }

    protected Operation assertBeanOperation(ObjectName name) {
        Operation op = getLastEntered();
        assertNotNull("No operation extracted", op);
        assertEquals("Mismatched bean name", name.getCanonicalName(), op.get(JmxPluginRuntimeDescriptor.BEAN_NAME_PROP, String.class));
        return op;
    }

    public static final Map<ObjectName, MBeanInfo> getBeansMap(MBeanServer server)
            throws IntrospectionException, InstanceNotFoundException, ReflectionException {
        Collection<ObjectName> names = listBeans(server);
        if (ListUtil.size(names) <= 0) {
            return Collections.emptyMap();
        }

        Map<ObjectName, MBeanInfo> map = new TreeMap<ObjectName, MBeanInfo>(BY_CANONICAL_NAME_COMPARATOR);
        for (ObjectName n : names) {
            MBeanInfo info = server.getMBeanInfo(n);
            if (info == null) {
                continue;
            }

            MBeanInfo prev = map.put(n, info);
            assertNullValue("Multiple information for " + n, prev);
        }

        return map;
    }

    public static final Set<ObjectName> listBeans(MBeanServer server) {
        assertNotNull("No server available", server);
        return server.queryNames(null, null);
    }
}
