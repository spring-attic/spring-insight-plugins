/**
 * Copyright 2009-2010 the original author or authors.
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


package com.springsource.insight.plugin.jdbc;

import java.util.List;

import com.springsource.insight.intercept.metrics.AbstractMetricsGenerator;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;

/**
 * Generates commands to create
 * jdbcExecutionTime and queryCount metrics.
 */
public abstract class JdbcMetricsGenerator extends AbstractMetricsGenerator {

	protected JdbcMetricsGenerator(OperationType type) {
		super(type);
	}
	
	@Override
	protected List<Frame> getExternalFramesForMetricGeneration(Trace trace) {
		return trace.getLastFramesOfType(opType);
	}
}