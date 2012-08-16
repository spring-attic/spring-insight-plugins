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

package com.springsource.insight.plugin.gemfire;

import com.springsource.insight.intercept.operation.OperationType;

public interface GemFireDefenitions {

	static final GemFireType  TYPE_QUERY = new GemFireType("Query", OperationType.valueOf("gemfire_query_operation"));
	
	static final String GEMFIRE = "GemFire";
	
	static final String FIELD_QUERY = "query";
	static final String FIELD_SERVERS = "servers";
	static final String FIELD_PATH = "fullPath";
	
	class GemFireType {
		private final String label;
		private final OperationType type;
		
		public GemFireType(String lavelValue, OperationType typeValue) {
			super();
			this.label = lavelValue;
			this.type = typeValue;
		}
		public String getLabel() {
			return label;
		}
		public OperationType getType() {
			return type;
		}
	}
	
}
