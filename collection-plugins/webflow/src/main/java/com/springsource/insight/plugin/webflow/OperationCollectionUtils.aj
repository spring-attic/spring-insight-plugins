package com.springsource.insight.plugin.webflow;

import java.util.Map;
import java.util.Set;

import org.springframework.webflow.action.EvaluateAction;
import org.springframework.webflow.action.SetAction;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.AnnotatedAction;

import com.springsource.insight.intercept.operation.OperationMap;


public privileged aspect OperationCollectionUtils {
	/*
	 * get action's expression string
	 * @param Action
	 */
	public static String getActionExpression(Action p_action) {
		String result=null;
		String expression=null;
		
		Action action=((AnnotatedAction)p_action).getTargetAction();
		if (action instanceof EvaluateAction) {
			//evaluate action
			EvaluateAction evalAction = (EvaluateAction)action;
			expression=evalAction.expression.toString();
			try {
				if (evalAction.resultExpression!=null)
					result=evalAction.resultExpression.toString();
			}
			catch(Error e) {
				// evalAction.resultExpression is not exists in prev webflow API
				
				String tmp[]=evalAction.toString().split("(result =|,)");
				if (tmp.length>2 && !"[null]".equals(tmp[2])) {
					result=tmp[2];
				}
			}
		}
		else
		if (action instanceof SetAction) {
			// set action
			SetAction setAction = (SetAction)action;
			result=setAction.nameExpression.toString();
			expression=setAction.valueExpression.toString();
		}
		else {
			expression=action.toString();
		}
		
		if (result!=null)
			expression=result+"="+expression;
		
		return expression;
	}
	
	public static void putAll(OperationMap opMap, Map<String,Object> map) {
		if (map!=null) {
			Set<Map.Entry<String, Object>> entries=map.entrySet();
			for(Map.Entry<String, Object> item: entries) {
				Object value=item.getValue();
				opMap.put(item.getKey(), (value!=null)?value.toString():null);
			}
		}
	}
}
