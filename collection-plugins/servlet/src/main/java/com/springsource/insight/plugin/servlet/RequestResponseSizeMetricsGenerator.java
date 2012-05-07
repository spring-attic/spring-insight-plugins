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
package com.springsource.insight.plugin.servlet;

import java.util.Collection;
import java.util.Collections;

import com.springsource.insight.intercept.metrics.AbstractMetricsGenerator;
import com.springsource.insight.intercept.metrics.MetricsBag;
import com.springsource.insight.intercept.metrics.MetricsBag.PointType;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.resource.ResourceKey;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.DataPoint;
import com.springsource.insight.util.time.TimeRange;
import com.springsource.insight.util.time.TimeUtil;

/**
 * Generates an {@link MetricsBag} for the request and response size of the
 * endpoint within a {@link Trace}
 */
public class RequestResponseSizeMetricsGenerator extends AbstractMetricsGenerator {

	public RequestResponseSizeMetricsGenerator() {
		super(OperationType.HTTP);
	}

	// TODO the "type" value should be using the same MetricDataType(s) literals (which should be exposed)
	public static final String ENDPOINT_RESPONSE_SIZE = "endPointResponseSize:type=bytes";
	public static final String ENDPOINT_REQUEST_SIZE = "endPointRequestSize:type=bytes";    


	@Override
    protected Collection<MetricsBag> addExtraEndPointMetrics(Trace trace, ResourceKey defaultKey, Collection<Frame> externalFrames) {
		Frame frame = trace.getFirstFrameOfType(opType);
		if (frame == null) {
		    return Collections.emptyList();
		}

		TimeRange   range = trace.getRange();
        int         time = TimeUtil.nanosToSeconds(range.getStart());
        Operation   op = frame.getOperation();
        ResourceKey resourceKey = getResourceKey(op, defaultKey);
		MetricsBag  mb = MetricsBag.create(resourceKey, range);
		mb.add(ENDPOINT_RESPONSE_SIZE, PointType.GAUGE);
		mb.add(ENDPOINT_REQUEST_SIZE, PointType.GAUGE);
		

		// Add the response size data point
		OperationMap response = op.get("response", OperationMap.class);
		Number contentSize = response.get("contentSize", Number.class);
		DataPoint responseSizePoint = new DataPoint(time, contentSize.doubleValue());
		mb.add(responseSizePoint, ENDPOINT_RESPONSE_SIZE);

		// Add the request size data point
		OperationMap request = op.get("request", OperationMap.class);
		Number contentLength = request.get("contentLength", Number.class);
		DataPoint requestSizePoint = new DataPoint(time, contentLength.doubleValue());
		mb.add(requestSizePoint, ENDPOINT_REQUEST_SIZE);
		return Collections.singletonList(mb);
	}

}
