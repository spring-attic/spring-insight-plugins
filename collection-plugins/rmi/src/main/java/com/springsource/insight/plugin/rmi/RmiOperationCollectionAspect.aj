/**
 * Copyright 2009-2011 the original author or authors.
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

package com.springsource.insight.plugin.rmi;

import java.rmi.Remote;
import java.rmi.registry.Registry;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.StringFormatterUtils;
import com.springsource.insight.util.StringUtil;

/**
 *
 */
public aspect RmiOperationCollectionAspect extends MethodOperationCollectionAspect {
	public RmiOperationCollectionAspect () {
		super();
	}

	public pointcut lookup() : execution (* Registry.lookup(String)) ;
	public pointcut bind() : execution (* Registry.bind(String, Remote)) ;
	public pointcut rebind() : execution (* Registry.rebind(String, Remote)) ;
	public pointcut unbind() : execution (* Registry.unbind(String)) ;
	
	public pointcut collectionPoint() : lookup() || bind() || rebind() || unbind();
	
	@Override
	protected Operation createOperation(JoinPoint jp) {
		Signature sig = jp.getSignature();
		String	action = sig.getName();
		String name = (String) jp.getArgs()[0];
		return super.createOperation(jp)
					.type(RmiDefinitions.RMI_ACTION)
					.label(/* TODO StringUtil.capitalize(action) */ action + " " + StringUtil.chopTailAndEllipsify(name, StringFormatterUtils.MAX_PARAM_LENGTH))
					.put(RmiDefinitions.ACTION_ATTR, action)
					.put(RmiDefinitions.NAME_ATTR, name)
					;
	}
	
	

	@Override
	public String getPluginName() {
		return RmiPluginRuntimeDescriptor.PLUGIN_NAME;
	}

}
