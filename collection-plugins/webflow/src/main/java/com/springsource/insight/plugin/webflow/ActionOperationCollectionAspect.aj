package com.springsource.insight.plugin.webflow;

import org.aspectj.lang.JoinPoint;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.AnnotatedAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;

/**
 * This aspect create insight operation for Webflow Action activity
 * @type: wf-action
 * @properties: action
 */
public privileged aspect ActionOperationCollectionAspect extends AbstractOperationCollectionAspect {
	    public pointcut collectionPoint() 
	    	: execution(Event org.springframework.webflow.execution.ActionExecutor.execute(Action, RequestContext));

	    protected Operation createOperation(JoinPoint jp) {
	    	Object[] args = jp.getArgs();
	    	String expression=OperationCollectionUtils.getActionExpression((AnnotatedAction)args[0]);
	        
	    	return new Operation().type(OperationCollectionTypes.ACTION_TYPE.type)
	    						.label(OperationCollectionTypes.ACTION_TYPE.label+" ["+expression+"]")
	    						.sourceCodeLocation(getSourceCodeLocation(jp))
	            	            .put("action", expression);
	    }
}
