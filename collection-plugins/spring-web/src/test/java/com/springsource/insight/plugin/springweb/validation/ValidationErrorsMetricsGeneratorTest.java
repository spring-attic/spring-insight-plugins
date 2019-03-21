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

package com.springsource.insight.plugin.springweb.validation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.springsource.insight.collection.test.AbstractCollectionTestSupport;
import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.metrics.MetricsBag;
import com.springsource.insight.intercept.metrics.MetricsBag.PointType;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.resource.ResourceKey;
import com.springsource.insight.intercept.resource.ResourceNames;
import com.springsource.insight.intercept.server.ServerName;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameId;
import com.springsource.insight.intercept.trace.SimpleFrame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;
import com.springsource.insight.util.IDataPoint;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.time.TimeRange;

/**
 * 
 */
public class ValidationErrorsMetricsGeneratorTest extends AbstractCollectionTestSupport {
	private static final ValidationErrorsMetricsGenerator	generator=ValidationErrorsMetricsGenerator.getInstance();
	private static final TimeRange	range=new TimeRange(7365L, 3777347L);

	public ValidationErrorsMetricsGeneratorTest() {
		super();
	}

	@Test
	public void testNoErrorsCount () {
		assertMetricsResult(createTrace(createValidationFrame(0)), "testNoErrorsCount", 0);
	}

	@Test
	public void testSimpleErrorsCount () {
		final int	NUM_ERRORS=47;
		assertMetricsResult(createTrace(createValidationFrame(NUM_ERRORS)), "testSimpleErrorsCount", NUM_ERRORS);
	}

	@Test
	public void testMultipleErrorCounts () {
		final int[]	counts={ 1, 2, 3, 5, 7, 11, 13, 17, 19, 23 };
		List<Frame>	children=new ArrayList<Frame>(counts.length);
		int			totalCount=0;
		for (int	errCount : counts) {
			children.add(createValidationFrame(errCount));
			totalCount += errCount;
		}

		Frame	root=new SimpleFrame(FrameId.valueOf(7365L), null, new Operation().label("root"), range, children);
		for (Frame child : children) {
			Mockito.when(child.getParent()).thenReturn(root);
		}
		
		assertMetricsResult(createTrace(root), "testMultipleErrorCounts", totalCount);
	}

	static MetricsBag assertMetricsResult (Trace trace, String endpointName, int expectedCount) {
		ResourceKey			resourceKey=ResourceKey.valueOf(ResourceNames.EndPoint, endpointName);
		List<MetricsBag>	mbList=generator.generateMetrics(trace, resourceKey);
		if (expectedCount <= 0) {
			assertEquals("Unepected metrics generated: " + mbList, 0, ListUtil.size(mbList));
			return null;
		}

		assertEquals("Unepected metrics generated: " + mbList, 1, ListUtil.size(mbList));
		
		MetricsBag	mb=mbList.get(0);
		assertEquals("Mismatched resource key", resourceKey, mb.getResourceKey());

		List<String>	metricNames=mb.getMetricKeys();
		assertEquals("Mismatched number of metrics: " + metricNames, 1, ListUtil.size(metricNames));

		String	metricKey=metricNames.get(0);
		assertEquals("Mismatched metric key", ValidationErrorsMetricsGenerator.METRIC_KEY, metricKey);
		assertEquals("Mismatched counter type", PointType.COUNTER, mb.getMetricType(metricKey));

		List<IDataPoint>	dpList=mb.getPoints(metricKey);
		assertEquals("Mismatched number of points: " + dpList, 1, ListUtil.size(dpList));

		IDataPoint	dp=dpList.get(0);
		assertEquals("Mismatched errors count", expectedCount, Math.round(dp.getValue()));

		return mb;
	}

	static Trace createTrace (Frame root) {
		return new Trace(ServerName.valueOf("srv"), ApplicationName.valueOf("app"), new Date(), TraceId.valueOf("id"), root);
	}

	static Frame createValidationFrame (int numErrors) {
		Operation	op=new Operation()
							.label(numErrors + " errors")
							.type(ValidationErrorsMetricsGenerator.TYPE)
							.put(ValidationJoinPointFinalizer.ERRORS_COUNT, numErrors);
		Frame	frame=createMockOperationWrapperFrame(op);
		Mockito.when(frame.getRange()).thenReturn(range);
		return frame;
	}
}
