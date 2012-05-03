package com.springsource.insight.plugin.struts2;

import org.aspectj.lang.JoinPoint;

import com.opensymphony.xwork2.ActionInvocation;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;

/**
 * Collection operation for Struts2 custom interceptors,
 * Do not collect internal Struts2 interceptors.
 */
public privileged aspect InterceptOperationCollectionAspect extends AbstractOperationCollectionAspect {
    public pointcut collectionPoint() :
    	execution(String com.opensymphony.xwork2.interceptor.Interceptor+.intercept(ActionInvocation))
    	// do not collect internal Struts2 interceptors
		&& !within(com.opensymphony.xwork2.interceptor..*) && !within(org.apache.struts2.interceptor..*);

    protected Operation createOperation(JoinPoint jp) {
    	String interceptor=jp.getThis().getClass().getName();
        
    	return new Operation().type(OperationCollectionTypes.INTERCEPT_TYPE.type)
    						.label(OperationCollectionTypes.INTERCEPT_TYPE.label+" ["+interceptor+"]")
    						.sourceCodeLocation(getSourceCodeLocation(jp))
    						.put("interceptor",interceptor);  // interceptor signature: ClassName
    }
	    
	@Override
    public String getPluginName() {
		return "struts2";
	}
}
