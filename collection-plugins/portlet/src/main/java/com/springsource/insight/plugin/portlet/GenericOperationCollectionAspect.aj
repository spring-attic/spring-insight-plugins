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

package com.springsource.insight.plugin.portlet;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletRequest;
import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;

/**
 * This aspect create insight operation for general Portlet
 */
abstract aspect GenericOperationCollectionAspect extends AbstractOperationCollectionAspect {
	
	@Override
    public String getPluginName() {
        return "portlet";
    }

	protected Operation createOperation(JoinPoint jp, OperationCollectionTypes opType) {
		String portletName=null;
		if (jp.getThis() instanceof GenericPortlet) {
			GenericPortlet portlet=(GenericPortlet)jp.getThis();
			portletName=portlet.getPortletName();
		}
		
		Object[] args = jp.getArgs();
		PortletRequest req=(PortletRequest)args[0];
		Map<String, String[]> preferences=req.getPreferences().getMap();
		Map<String, String[]> params=req.getParameterMap();
        
		Operation operation=new Operation().type(opType.type)
    						.label("Portlet: '"+portletName+"'"+" ["+opType.label+"]")
    						.sourceCodeLocation(getSourceCodeLocation(jp))
    						.put("name", portletName)
            	            .put("mode", req.getPortletMode().toString())
            	            .put("winState", req.getWindowState().toString());
		
		try {
			//portlet2 support
			operation.put("winId", req.getWindowID());
		}
		catch(Error e) {
		}
		
		createMap(operation,"preferences",preferences);
		createMap(operation,"params",params);
		
		return operation;
	}
	
	private void createMap(Operation op, String name, Map<String, String[]> values) {
		if (values!=null && !values.isEmpty()) {
			OperationMap opMap=op.createMap(name);
			
			Set<Map.Entry<String,String[]>> entries=values.entrySet();
			for(Map.Entry<String,String[]> item: entries) {
				String[] value=item.getValue();
				opMap.put(item.getKey(), (value!=null)?Arrays.toString(value):null);
			}
		}
	}
}
