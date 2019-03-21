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

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationUtils;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringFormatterUtils;


/**
 *
 */
public abstract class JmxMultiAttributeCollectionTestSupport extends JmxSingleAttributeOperationTestSupport {

    protected JmxMultiAttributeCollectionTestSupport(String actionName) {
        super(actionName);
    }

    protected Operation assertAttributesListOperation(ObjectName name, AttributeList attrs) {
        return assertAttributeOperation(name, JmxMultiAttributeOperationCollectionSupport.createNamesList(attrs));
    }

    protected Operation assertAttributesListOperation(ObjectName name, String... attrsNames) {
        return assertAttributeOperation(name, JmxMultiAttributeOperationCollectionSupport.createNamesList(attrsNames));
    }

    protected OperationList assertEncodedManagedAttributes(Operation op, ObjectName name, AttributeList values) {
        assertNotNull(name.getCanonicalName() + ": No attributes operation", op);
        return assertEncodedManagedAttributes(op.get(JmxPluginRuntimeDescriptor.ATTR_LIST_PROP, OperationList.class), name, values);
    }

    protected OperationList assertEncodedManagedAttributes(OperationList op, ObjectName name, AttributeList values) {
        assertNotNull(name.getCanonicalName() + ": No encoded attributes list", op);
        assertEquals(name.getCanonicalName() + ": mismatched attributes count", ListUtil.size(values), op.size());

        for (int index = 0; index < op.size(); index++) {
            Attribute expected = (Attribute) values.get(index);
            String expName = expected.getName(), expValue = StringFormatterUtils.formatObject(expected.getValue());
            OperationMap actual = op.get(index, OperationMap.class);
            assertNotNull(name.getCanonicalName() + "[" + expName + "]: no actual encoding", actual);
            assertEquals(name.getCanonicalName() + ": Mismatched encoded name", expName, actual.get(OperationUtils.NAME_KEY, String.class));
            assertEquals(name.getCanonicalName() + "[" + expName + "]: Mismatched encoded value", expValue, actual.get(OperationUtils.VALUE_KEY, String.class));
        }

        return op;
    }

    protected AttributeList getSingleAttribute(MBeanServer server, ObjectName name, String attrName)
            throws InstanceNotFoundException, ReflectionException {
        return server.getAttributes(name, new String[]{attrName});
    }

    public static final List<String> getReadableAttributes(MBeanInfo info) {
        return getReadableAttributes(info.getAttributes());
    }

    public static final List<String> getReadableAttributes(MBeanAttributeInfo... attrs) {
        return getAccessibleAttributes(Boolean.TRUE, attrs);
    }

    public static final List<String> getWriteableAttributes(MBeanInfo info) {
        return getWriteableAttributes(info.getAttributes());
    }

    public static final List<String> getWriteableAttributes(MBeanAttributeInfo... attrs) {
        return getAccessibleAttributes(Boolean.FALSE, attrs);
    }

    public static final List<String> getAccessibleAttributes(Boolean readOrWrite, MBeanAttributeInfo... attrs) {
        if (ArrayUtil.length(attrs) <= 0) {
            return Collections.emptyList();
        }

        List<String> names = new ArrayList<String>(attrs.length);
        for (MBeanAttributeInfo info : attrs) {
            String n = info.getName();
            if (readOrWrite != null) {
                if (readOrWrite.booleanValue()) {
                    if (!info.isReadable()) {
                        continue;
                    }
                } else {
                    if (!info.isWritable()) {
                        continue;
                    }
                }
            }

            names.add(n);
        }

        return names;
    }
}
