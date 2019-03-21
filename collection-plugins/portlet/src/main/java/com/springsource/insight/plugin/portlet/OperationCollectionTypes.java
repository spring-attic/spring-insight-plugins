/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.insight.plugin.portlet;

import com.springsource.insight.intercept.operation.OperationType;


public enum OperationCollectionTypes {
	ACTION_TYPE("portlet-action", "Action"),
	EVENT_TYPE("portlet-event", "Event"),
	RENDER_TYPE("portlet-render", "Render"),
	RESOURCE_TYPE("portlet-resource", "ResourceServing");


	final public OperationType type;
	final public String label;

	private OperationCollectionTypes(String typeId, String typeLabel) {
		this.type=OperationType.valueOf(typeId);
		this.label = typeLabel;
	}
}