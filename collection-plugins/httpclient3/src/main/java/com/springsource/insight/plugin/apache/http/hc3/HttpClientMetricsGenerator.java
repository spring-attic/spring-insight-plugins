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

package com.springsource.insight.plugin.apache.http.hc3;

import java.util.Collection;

import com.springsource.insight.intercept.metrics.AbstractMetricsGenerator;
import com.springsource.insight.intercept.metrics.MetricsBag;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;

/**
 * Generates an {@link MetricsBag} for the request and response size of the
 * endpoint within a {@link Trace}
 */
public class HttpClientMetricsGenerator extends AbstractMetricsGenerator {

	public HttpClientMetricsGenerator() {
		super(HttpClientDefinitions.TYPE);
	}

	@Override
	protected Collection<Frame> getExternalFramesForMetricGeneration(Trace trace) {
		return trace.getAllFramesOfType(opType);
	}

}
