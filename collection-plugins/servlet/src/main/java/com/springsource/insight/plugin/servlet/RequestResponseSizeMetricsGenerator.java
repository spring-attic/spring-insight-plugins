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
package com.springsource.insight.plugin.servlet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.time.TimeRange;
import com.springsource.insight.util.time.TimeUtil;

/**
 * Generates an {@link MetricsBag} for the request and response size of the
 * endpoint within a {@link Trace}
 */
public class RequestResponseSizeMetricsGenerator extends AbstractMetricsGenerator {
	// TODO the "type" value should be using the same MetricDataType(s) literals (which should be exposed)
	public static final String ENDPOINT_RESPONSE_SIZE = "endPointResponseSize:type=bytes";
	public static final String ENDPOINT_REQUEST_SIZE = "endPointRequestSize:type=bytes";    

	public RequestResponseSizeMetricsGenerator() {
		super(OperationType.HTTP);
	}

	@Override
	public Collection<Frame> locateFrames(Trace trace) {
		Frame frame = trace.getFirstFrameOfType(getOperationType());
		if (frame == null) {
		    return Collections.emptyList();
		} else {
			return Collections.singletonList(frame);
		}
	}

	@Override
	public List<MetricsBag> generateMetrics(Trace trace, ResourceKey endpointResourceKey, Collection<Frame> frames) {
		if (ListUtil.size(frames) != 1) {
		    return Collections.emptyList();
		}

        Frame		frame = ListUtil.getFirstMember(frames);
        Operation   op = frame.getOperation();
        ResourceKey resourceKey = getResourceKey(op, endpointResourceKey);
		TimeRange   range = trace.getRange();
        int         time = TimeUtil.nanosToSeconds(range.getStart());
		MetricsBag  mb = MetricsBag.create(resourceKey, range);

		// Add the response size data point
		OperationMap response = op.get("response", OperationMap.class);
		Number contentSize = (response == null) ? null : response.get("contentSize", Number.class);
		if (contentSize != null) {	// OK if missing since collected only if extra information
			DataPoint responseSizePoint = new DataPoint(time, contentSize.doubleValue());
			mb.add(ENDPOINT_RESPONSE_SIZE, PointType.GAUGE);
			mb.add(responseSizePoint, ENDPOINT_RESPONSE_SIZE);
		}

		// Add the request size data point
		OperationMap request = op.get("request", OperationMap.class);
		Number contentLength = (request == null) ? null : request.get("contentLength", Number.class);
		if (contentLength != null) {	// OK if missing since collected only if extra information
			DataPoint requestSizePoint = new DataPoint(time, contentLength.doubleValue());
			mb.add(ENDPOINT_REQUEST_SIZE, PointType.GAUGE);
			mb.add(requestSizePoint, ENDPOINT_REQUEST_SIZE);
		}

		if (mb.isEmpty()) {
			return Collections.emptyList();
		} else {
			return Collections.singletonList(mb);
		}
	}
}
