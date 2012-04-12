package com.springsource.insight.plugin.webflow;

import com.springsource.insight.intercept.operation.OperationType;


public enum OperationCollectionTypes {
	START_TYPE("wf-start", "WebFlow: Start"),
	STATE_TYPE("wf-state", "WebFlow: "),
	ACTION_TYPE("wf-action", "WebFlow: Action"),
	TRANSITION_TYPE("wf-transition", "WebFlow: Transition");


	final public OperationType type;
	final public String label;

	private OperationCollectionTypes(String typeId, String label) {
		this.type=OperationType.valueOf(typeId);
		this.label = label;
	}
}
