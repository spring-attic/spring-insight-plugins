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

package com.springsource.insight.plugin.struts2;

import java.util.Map;
import java.util.Set;

import org.aspectj.lang.JoinPoint;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;

/**
 * Collection operation for Struts2 flow execution start  
 */
public privileged aspect StartOperationCollectionAspect extends AbstractOperationCollectionAspect {
    public StartOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint() : execution(String com.opensymphony.xwork2.ActionProxy+.execute());

    @Override
    protected Operation createOperation(JoinPoint jp) {
    	ActionProxy actionProxy=(ActionProxy)jp.getThis();
    	// get Struts action name 
		String actionName=actionProxy.getNamespace()+actionProxy.getActionName();
		
		ActionInvocation aci=actionProxy.getInvocation();
    	ActionContext ctx=aci.getInvocationContext();
    	// get requests parameters
		Map<String,Object> params=ctx.getParameters();
    	
		Operation operation = new Operation().type(OperationCollectionTypes.START_TYPE.type)
    						.label(OperationCollectionTypes.START_TYPE.label+" ["+actionName+"]")
    						.sourceCodeLocation(getSourceCodeLocation(jp))
    						.put("actionName", actionName);
		
		if (params!=null && !params.isEmpty()) {
			// add requests parameters
			OperationMap map=operation.createMap("params");			
			Set<Map.Entry<String,Object>> entries=params.entrySet();
			for(Map.Entry<String,Object> item: entries) {
				String[] value=(String[]) item.getValue();
				map.put(item.getKey(), (value!=null)?value[0]:null);
			}
		}
		
		return operation;
    }
    
	@Override
    public String getPluginName() {
		return Struts2PluginRuntimeDescriptor.PLUGIN_NAME;
	}
}
