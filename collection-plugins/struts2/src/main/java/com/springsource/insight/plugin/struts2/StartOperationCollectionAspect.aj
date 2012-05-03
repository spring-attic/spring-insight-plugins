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
    public pointcut collectionPoint() : execution(String com.opensymphony.xwork2.ActionProxy+.execute());

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
		return "struts2";
	}
}
