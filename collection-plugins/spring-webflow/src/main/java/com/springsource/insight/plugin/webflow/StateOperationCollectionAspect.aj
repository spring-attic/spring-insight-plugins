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

package com.springsource.insight.plugin.webflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.springframework.webflow.engine.ActionList;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.DecisionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.engine.builder.BinderConfiguration.Binding;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ViewFactory;
import org.springframework.webflow.mvc.view.AbstractMvcViewFactory;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;


/**
 * This aspect create insight operation for Webflow State activity
 * @type: wf-state
 * @properties: stateType, stateId, view, attribs<String,Object>
 * @properties: entryActions<String>, actions<String>, exitActions<String>
 * @properties: trans<String,String>
 */
public privileged aspect StateOperationCollectionAspect extends AbstractOperationCollectionAspect {
    public StateOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint() 
    	: execution(void org.springframework.webflow.engine.impl.FlowExecutionImpl.setCurrentState(State, RequestContext));

	@Override
    @SuppressWarnings("unchecked")
	protected Operation createOperation(JoinPoint jp) {
    	Object[] args = jp.getArgs();
    	
		ActionList actions=null;
		ActionList exitActions=null;
		BinderConfiguration bindConfig=null;
		String view=null;
		
		String stateType="FlowState";
		State state=(State)args[0];
		if (state instanceof TransitionableState) {
			exitActions=((TransitionableState)state).getExitActionList();
			
			if (state instanceof DecisionState) {
				stateType="DecisionState";
			}
			else
			if (state instanceof ActionState) {
				stateType="ActionState";
				actions=((ActionState)state).getActionList();
			}
			else
			if (state instanceof ViewState) {
				stateType="ViewState";
				actions=((ViewState)state).getRenderActionList();
				// get view
				ViewFactory viewFactoryInterface=((ViewState)state).getViewFactory();
				if (viewFactoryInterface instanceof AbstractMvcViewFactory) {
					AbstractMvcViewFactory viewFactory=(AbstractMvcViewFactory)viewFactoryInterface;
					view=viewFactory.viewId.toString();
					bindConfig=viewFactory.binderConfiguration;
				}
			}
		}
		else
		if (state instanceof EndState) {
			stateType="EndState";
		}
		
		Operation operation = new Operation().type(OperationCollectionTypes.STATE_TYPE.type)
											.label(OperationCollectionTypes.STATE_TYPE.label+stateType+" ["+state.getId()+"]")
	    									.sourceCodeLocation(getSourceCodeLocation(jp));
		operation.put("stateType",stateType)
				.put("stateId",state.getId());
		if (view!=null) {
			operation.put("view",view);
		}
		
		if (!state.getAttributes().isEmpty()) {
			operation.createMap("attribs").putAnyAll(state.getAttributes().asMap());
		}
		
		/*List<TransitionExecutingFlowExecutionExceptionHandler> excepts=state.getExceptionHandlerSet().exceptionHandlers;
		if (!excepts.isEmpty()) {
			OperationMap exList = operation.createMap("exceptions");
			for(Iterator<TransitionExecutingFlowExecutionExceptionHandler> i=excepts.iterator(); i.hasNext();) {
				Map<Class, DefaultTargetStateResolver> exsmap=i.next().exceptionTargetStateMappings;
				for(Class key: exsmap.keySet()) {
					exList.put(key.getName(), exsmap.get(key).targetStateIdExpression.toString());
				}
			}
		}*/
		
		// actions
		if (state.getEntryActionList().size()>0) {
			OperationList entryActionsOp = operation.createList("entryActions");
			entryActionsOp.addAll(actionList(state.getEntryActionList()));
		}
		
		if (actions!=null && actions.size()>0) {
			OperationList actionsOp = operation.createList("actions");
			actionsOp.addAll(actionList(actions));
		}
		
		if (exitActions!=null && exitActions.size()>0) {
			OperationList exitActionsOp = operation.createList("exitActions");
			exitActionsOp.addAll(actionList(exitActions));
		}
		
		if (bindConfig!=null) {
			OperationList binds = operation.createList("binds");
			for(Binding bind: (Set<Binding>)bindConfig.getBindings()) {
				String conv=bind.getConverter();
				binds.add(bind.getProperty()+(conv!=null?" ("+conv+")":""));
			}
		}
        
        return operation;
    }
    
	/**
	 * Return collection of actions
	 * @param ActionList
	 * @return Collection<? extends Object>
	 */
	@SuppressWarnings("unchecked")
	private static Collection<?> actionList(ActionList actions) {
	    if (actions == null) {
	        return Collections.emptyList();
	    }

	    List<String> actionList=new ArrayList<String>(actions.size());
	    for(Iterator<? extends Action> iterator=actions.iterator(); iterator.hasNext(); ) {
	        String expression=OperationCollectionUtils.getActionExpression(iterator.next());
	        actionList.add(expression);
	    }

		return actionList;
	}

    @Override
    public String getPluginName() {
        return WebflowPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
