package com.springsource.insight.plugin.webflow;

import java.util.Iterator;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.springframework.webflow.engine.RequestControlContext;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.TransitionCriteria;
import org.springframework.webflow.engine.support.ActionTransitionCriteria;
import org.springframework.webflow.engine.support.TransitionCriteriaChain;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;

/**
 * This aspect create insight operation for Webflow Transition activity
 * @type: wf-transition
 * @properties: codeId, stateId, attribs<String,Object>, actions<String>
 */
public privileged aspect TransitionOperationCollectionAspect extends AbstractOperationCollectionAspect {
	    public pointcut collectionPoint() 
	    	: execution(boolean org.springframework.webflow.engine.Transition.execute(State, RequestControlContext));

	    @SuppressAjWarnings("TypeNotExposedToWeaver")
		@SuppressWarnings("unchecked")
		protected Operation createOperation(JoinPoint jp) {
	    	Transition transition = (Transition)jp.getThis();
	        String expr=transition.getId()+" -> "+transition.getTargetStateId();
	    	
	    	Operation  operation = new Operation().type(OperationCollectionTypes.TRANSITION_TYPE.type)
	    										.label(OperationCollectionTypes.TRANSITION_TYPE.label+" ["+expr+"]")
	    										.sourceCodeLocation(getSourceCodeLocation(jp));
	    	
	    	operation.put("codeId", transition.getId())
	        		.put("stateId", transition.getTargetStateId());
	    	
	    	if (!transition.getAttributes().isEmpty()) {
	    		operation.createMap("attribs").putAnyAll(transition.getAttributes().asMap());
	    	}
	        
	    	// get transition's activities
	        TransitionCriteria execCriteria=transition.getExecutionCriteria();
			if (execCriteria instanceof TransitionCriteriaChain) {
				List<ActionTransitionCriteria> criterias=((TransitionCriteriaChain)execCriteria).criteriaChain;
				if (!criterias.isEmpty()) {
					OperationList actions = operation.createList("actions");
					for(Iterator<ActionTransitionCriteria> i=criterias.iterator(); i.hasNext();) {
						actions.add(OperationCollectionUtils.getActionExpression(i.next().action));
					}
				}
			}
	        
	        return operation;
	    }

    @Override
    public String getPluginName() {
        return "webflow";
    }
}
