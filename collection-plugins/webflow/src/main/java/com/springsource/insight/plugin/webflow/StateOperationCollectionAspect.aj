package com.springsource.insight.plugin.webflow;

import java.util.ArrayList;
import java.util.Collection;
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
import org.springframework.webflow.execution.AnnotatedAction;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.ViewFactory;
import org.springframework.webflow.mvc.view.AbstractMvcViewFactory;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;


/**
 * This aspect create insight operation for Webflow State activity
 * @type: wf-state
 * @properties: stateType, stateId, view, attribs<String,Object>
 * @properties: entryActions<String>, actions<String>, exitActions<String>
 * @properties: trans<String,String>
 */
public privileged aspect StateOperationCollectionAspect extends AbstractOperationCollectionAspect {
	    public pointcut collectionPoint() 
	    	: execution(void org.springframework.webflow.engine.impl.FlowExecutionImpl.setCurrentState(State, RequestContext));

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
				OperationMap attribs = operation.createMap("attribs");
				OperationCollectionUtils.putAll(attribs, state.getAttributes().asMap());
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
				entryActionsOp.addAll((Collection<Object>)actionList(state.getEntryActionList()));
			}
			
			if (actions!=null && actions.size()>0) {
				OperationList actionsOp = operation.createList("actions");
				actionsOp.addAll((Collection<Object>)actionList(actions));
			}
			
			if (exitActions!=null && exitActions.size()>0) {
				OperationList exitActionsOp = operation.createList("exitActions");
				exitActionsOp.addAll((Collection<Object>)actionList(exitActions));
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
		private static Collection<? extends Object> actionList(ActionList actions) {
	    	List<String> actionList=new ArrayList<String>();
			if (actions!=null) {
				for(Iterator<AnnotatedAction> iterator=actions.iterator(); iterator.hasNext(); ) {
					String expression=OperationCollectionUtils.getActionExpression(iterator.next());
			    	actionList.add(expression);
				}
			}
			return actionList;
		}

    @Override
    public String getPluginName() {
        return "webflow";
    }
}
