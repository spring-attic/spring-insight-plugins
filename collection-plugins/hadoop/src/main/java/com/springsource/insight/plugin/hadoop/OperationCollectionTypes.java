/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
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

package com.springsource.insight.plugin.hadoop;

import com.springsource.insight.intercept.operation.OperationType;

/**
 * Collection operations types for Hadoop 
 */
public enum OperationCollectionTypes {
	JOB_TYPE("hdp-job", "Hadoop Job: Start"),
	MAP_TYPE("hdp-mapper", "Hadoop Mapper"),
	REDUCE_TYPE("hdp-reducer", "Hadoop Reducer");


	final public OperationType type;
	final public String label;

	private OperationCollectionTypes(String typeId, String labelValue) {
		this.type=OperationType.valueOf(typeId);
		this.label = labelValue;
	}
	
	public String getValue() {
		return type.getName();
	}
}
