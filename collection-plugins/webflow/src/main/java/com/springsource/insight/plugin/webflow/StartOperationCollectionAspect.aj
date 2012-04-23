package com.springsource.insight.plugin.webflow;

import org.aspectj.lang.JoinPoint;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.RequestControlContext;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;

/**
 * This aspect create insight operation for Webflow Start activity
 * @type: wf-start
 * @properties: flowId, initParams<String,Object>
 */
public aspect StartOperationCollectionAspect extends AbstractOperationCollectionAspect {
	    public pointcut collectionPoint() 
	    	: execution(void org.springframework.webflow.engine.impl.FlowExecutionImpl.start(Flow, MutableAttributeMap, RequestControlContext));

	    @SuppressWarnings("unchecked")
		protected Operation createOperation(JoinPoint jp) {
	    	Object[] args = jp.getArgs();
	    	Flow flow = (Flow)args[0];
	    	MutableAttributeMap params=(MutableAttributeMap)args[1];
	        
	    	Operation  operation = new Operation().type(OperationCollectionTypes.START_TYPE.type)
	    										.label(OperationCollectionTypes.START_TYPE.label+" ["+flow.getId()+"]")
	    										.sourceCodeLocation(getSourceCodeLocation(jp));
	            
	        operation.put("flowId", flow.getId());
	        
	        if (params!=null && !params.isEmpty()) {
	        	OperationMap initParams = operation.createMap("initParams");
	        	OperationCollectionUtils.putAll(initParams, params.asMap());
	        }
	        
	        return operation;
	    }

    @Override
    public String getPluginName() {
        return "webflow";
    }
}
