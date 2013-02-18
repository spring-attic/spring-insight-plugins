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

import javax.management.ObjectName;

import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public abstract class JmxSingleAttributeOperationTestSupport extends JmxAttributeOperationTestSupport {
	protected JmxSingleAttributeOperationTestSupport(String actionName) {
		super(actionName);
	}

	protected Operation assertAttributeOperation(ObjectName name, String attrName) {
		Operation	op=assertBeanOperation(name);
		assertEquals("Mismatched operation type", JmxPluginRuntimeDescriptor.ATTR, op.getType());
		assertEquals("Mismatched label", JmxSingleAttributeOperationCollectionSupport.createLabel(action, attrName), op.getLabel());
		assertEquals("Mismatched action", action, op.get(JmxPluginRuntimeDescriptor.ACTION_PROP, String.class));
		assertEquals("Mismatched attribute name", attrName, op.get(JmxPluginRuntimeDescriptor.ATTR_NAME_PROP, String.class));
		return op;
	}
}
