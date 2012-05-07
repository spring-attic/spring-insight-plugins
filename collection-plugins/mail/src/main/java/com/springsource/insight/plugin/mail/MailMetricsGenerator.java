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

package com.springsource.insight.plugin.mail;

import java.util.List;

import com.springsource.insight.intercept.metrics.AbstractMetricsGenerator;
import com.springsource.insight.intercept.metrics.MetricsBag;
import com.springsource.insight.intercept.metrics.MetricsBag.PointType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.DataPoint;
import com.springsource.insight.util.time.TimeUtil;

public class MailMetricsGenerator extends AbstractMetricsGenerator {

	public static final String  MAIL_SIZE_METRIC = "mailSize:type=bytes";
	
	public MailMetricsGenerator() {
		super(MailDefinitions.SEND_OPERATION);
	}

	@Override
	protected void addExtraExternalResourceMetrics(Trace trace, Frame opTypeFrame, MetricsBag mb) {
		// Add the message size data point
		Number contentSize = (Number) opTypeFrame.getOperation().get("size");
		// OK if missing - the size is collected only if extra information is enabled
		if (contentSize == null) {
		    return;
		}
        mb.add(MAIL_SIZE_METRIC, PointType.GAUGE);
		int time = TimeUtil.nanosToSeconds(trace.getRange().getStart());
		DataPoint responseSizePoint = new DataPoint(time, contentSize.doubleValue());
		mb.add(responseSizePoint, MAIL_SIZE_METRIC);		
	}

	@Override
	protected List<Frame> getExternalFramesForMetricGeneration(Trace trace) {
		return trace.getLastFramesOfType(opType);
	}
}
