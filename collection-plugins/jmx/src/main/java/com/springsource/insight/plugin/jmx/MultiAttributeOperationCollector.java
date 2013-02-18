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

import javax.management.Attribute;
import javax.management.AttributeList;

import com.springsource.insight.collection.DefaultOperationCollector;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationUtils;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringFormatterUtils;

/**
 * A special {@link OperationCollector} that handles an {@link AttributeList}
 * return value
 */
public class MultiAttributeOperationCollector extends DefaultOperationCollector {
	public MultiAttributeOperationCollector() {
		super();
	}

	@Override
	protected void processNormalExit(Operation op, Object returnValue) {
		if (returnValue instanceof AttributeList) {
			encodeManagedAttributes(op, (AttributeList) returnValue);
		}
	}

	static OperationList encodeManagedAttributes(Operation op, AttributeList attrList) {
		return encodeManagedAttributes(op.createList(JmxPluginRuntimeDescriptor.ATTR_LIST_PROP), attrList);
	}
	
	static OperationList encodeManagedAttributes(OperationList op, AttributeList attrList) {
		if (ListUtil.size(attrList) <= 0) {
			op.clear();
			return op;
		}
		
		for (Object a : attrList) {
			Attribute	attr=(Attribute) a;
			OperationUtils.addNameValuePair(op, attr.getName(), StringFormatterUtils.formatObject(attr.getValue()));
		}
		
		return op;
	}
}
