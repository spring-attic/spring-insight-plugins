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

import com.springsource.insight.intercept.operation.OperationType;


public enum OperationCollectionTypes {
	START_TYPE("wf-start", "WebFlow: Start"),
	STATE_TYPE("wf-state", "WebFlow: "),
	ACTION_TYPE("wf-action", "WebFlow: Action"),
	TRANSITION_TYPE("wf-transition", "WebFlow: Transition");


	final public OperationType type;
	final public String label;

	private OperationCollectionTypes(String typeId, String labelValue) {
		this.type=OperationType.valueOf(typeId);
		this.label = labelValue;
	}
}
