package com.springsource.insight.plugin.struts2;

import com.springsource.insight.intercept.operation.OperationType;

/**
 * Collection operations types for Struts2 
 */
public enum OperationCollectionTypes {
	START_TYPE("str2-start", "Struts2 Flow: Start"),
	INTERCEPT_TYPE("str2-intercept", "Struts2 Flow: Interception"),
	ACTION_TYPE("str2-action", "Struts2 Flow: Action"),
	RESULT_TYPE("str2-result", "Struts2 Flow: Result");


	final public OperationType type;
	final public String label;

	private OperationCollectionTypes(String typeId, String label) {
		this.type=OperationType.valueOf(typeId);
		this.label = label;
	}
	
	public String getValue() {
		return type.getName();
	}
}
