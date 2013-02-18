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
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.StringUtil;

/**
 * Serves as base class for aspects that instrument get/set attribute(s)
 */
public abstract aspect JmxAttributeOperationCollectionSupport extends JmxOperationCollectionAspectSupport {
	protected final String	action;

	protected JmxAttributeOperationCollectionSupport(String actionName, OperationCollector collector) {
		super(collector);

		if (StringUtil.isEmpty(actionName)) {
			throw new IllegalArgumentException("No action specified");
		}
		
		action = actionName;
	}

	protected JmxAttributeOperationCollectionSupport(String actionName) {
		if (StringUtil.isEmpty(actionName)) {
			throw new IllegalArgumentException("No action specified");
		}
		
		action = actionName;
	}

	// need to use 'call' since some implementations come from the core
	public pointcut getAttributeValue()
		: (call(* MBeanServer+.getAttribute(ObjectName,String)))
	   || (call(* MBeanServerConnection+.getAttribute(ObjectName,String)))
	    ;

	public pointcut setAttributeValue()
		: (call(* MBeanServer+.setAttribute(ObjectName,Attribute)))
	   || (call(* MBeanServerConnection+.setAttribute(ObjectName,Attribute)))
	    ;

	public pointcut getAttributesList()
		: (call(* MBeanServer+.getAttributes(ObjectName,String[])))
	   || (call(* MBeanServerConnection+.getAttributes(ObjectName,String[])))
	    ;

	public pointcut setAttributesList()
		: (call(* MBeanServer+.setAttributes(ObjectName,AttributeList)))
	   || (call(* MBeanServerConnection+.setAttributes(ObjectName,AttributeList)))
	    ;

	protected Operation createAttributeOperation(JoinPoint jp, ObjectName name) {
		return createBeanOperation(jp, name)
							.type(JmxPluginRuntimeDescriptor.ATTR)
							.put(JmxPluginRuntimeDescriptor.ACTION_PROP, action);
	}
	
	protected String getAttributeName(JoinPoint jp) {
		return getAttributeName(jp.getArgs());
	}
	
	protected String getAttributeName(Object...args) {
		return ArrayUtil.findFirstInstanceOf(String.class, args);
	}

}
