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
import javax.management.ObjectName;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringFormatterUtils;


/**
 * Serves as base class for aspects that instrument multiple attributes set/get
 */
public abstract aspect JmxMultiAttributeOperationCollectionSupport extends JmxSingleAttributeOperationCollectionSupport {
	protected JmxMultiAttributeOperationCollectionSupport(String actionName) {
		super(actionName);
	}

	protected JmxMultiAttributeOperationCollectionSupport(String actionName, OperationCollector collector) {
		super(actionName, collector);
	}
	
	protected Operation createAttributeOperation(JoinPoint jp, ObjectName name, String ...attrs) {
		return createAttributeOperation(jp, name, createNamesList(attrs));
	}

	protected Operation createAttributeOperation(JoinPoint jp, ObjectName name, AttributeList attrs) {
		return createAttributeOperation(jp, name, createNamesList(attrs));
	}

	static final String createNamesList(AttributeList attrs) {
		int			numAttrs=ListUtil.size(attrs);
		String[]	attrsNames=new String[numAttrs];
		for (int index=0; index < numAttrs; index++) {
			Attribute	attr=(Attribute) attrs.get(index);
			attrsNames[index] = attr.getName();
		}
		
		return createNamesList(attrsNames);
	}

	static final String createNamesList(String ... names) {
		return StringFormatterUtils.arrayToDelimitedString(names);
	}
}
