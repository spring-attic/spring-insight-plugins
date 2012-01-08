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

package com.springsource.insight.plugin.rabbitmqClient;

import java.util.List;

import com.springsource.insight.intercept.metrics.AbstractMetricsGenerator;
import com.springsource.insight.intercept.metrics.MetricsBag;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;

abstract class AbstractRabbitMetricsGenerator extends AbstractMetricsGenerator {
	public static final String RABBIT_COUNT_SUFFIX = "rabbitMQ:type=counter";

	private RabbitPluginOperationType rabbitOpType;

	AbstractRabbitMetricsGenerator(RabbitPluginOperationType rabbitOpType) {
		super(rabbitOpType.getOperationType());
		this.rabbitOpType = rabbitOpType;
	}

	@Override
	protected void addExtraEndPointMetrics(Trace trace, MetricsBag mb, Boolean atLeastOneExternal) {
		if (atLeastOneExternal){
			addCounterMetricToBag(trace, mb, createMetricKey(), 1);
		}
	}

	@Override
	protected void addExtraExternalResourceMetrics(Trace trace, MetricsBag mb) {
		addCounterMetricToBag(trace, mb, createMetricKey(), 1);
	}

	@Override
	protected List<Frame> getExternalFramesForMetricGeneration(Trace trace) {
		return trace.getLastFramesOfType(opType);
	}

	String createMetricKey() {
		return rabbitOpType.name() + RABBIT_COUNT_SUFFIX;
	}
}
