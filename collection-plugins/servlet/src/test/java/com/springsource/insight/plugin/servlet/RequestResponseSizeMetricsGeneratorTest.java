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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.springsource.insight.collection.test.AbstractCollectionTestSupport;
import com.springsource.insight.intercept.metrics.MetricsBag;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.resource.ResourceKey;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameId;
import com.springsource.insight.intercept.trace.SimpleFrame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.IDataPoint;
import com.springsource.insight.util.time.TimeRange;

public class RequestResponseSizeMetricsGeneratorTest extends AbstractCollectionTestSupport {
    private final TimeRange timeRange = new TimeRange(1304387418963003000l, 1304387419123224000l);
    private final RequestResponseSizeMetricsGenerator gen = RequestResponseSizeMetricsGenerator.getInstance();

    public RequestResponseSizeMetricsGeneratorTest() {
        super();
    }

    @Test
    public void generateMetrics() {
        Trace trace = mock(Trace.class);
        when(trace.getFirstFrameOfType(OperationType.HTTP)).thenReturn(makeHttpFrame());
        when(trace.getRange()).thenReturn(timeRange);

        List<MetricsBag> mbs = gen.generateMetrics(trace, ResourceKey.valueOf("EndPoint", "epName"));
        assertEquals(1, mbs.size());

        MetricsBag mb = mbs.get(0);

        List<String> keys = mb.getMetricKeys();
        assertEquals(2, keys.size());
        boolean foundReq = false, foundRes = false;
        for (String key : keys) {
            if (!foundReq)
                foundReq = key.equals(RequestResponseSizeMetricsGenerator.ENDPOINT_REQUEST_SIZE) ? true : false;
            if (!foundRes)
                foundRes = key.equals(RequestResponseSizeMetricsGenerator.ENDPOINT_RESPONSE_SIZE) ? true : false;
        }
        assertTrue(foundRes && foundReq);

        List<IDataPoint> points = mb.getPoints(RequestResponseSizeMetricsGenerator.ENDPOINT_REQUEST_SIZE);
        assertEquals(1, points.size());
        assertEquals(1000.0, points.get(0).getValue(), 0.01);

        points = mb.getPoints(RequestResponseSizeMetricsGenerator.ENDPOINT_RESPONSE_SIZE);
        assertEquals(1, points.size());
        assertEquals(4000.0, points.get(0).getValue(), 0.01);

        // Assert that all keys are actually contained in the bag
        for (String key : keys) {
            mb.getPoints(key);
        }
    }

    @Test
    public void noHttpFrame() {
        Trace trace = mock(Trace.class);
        when(trace.getFirstFrameOfType(OperationType.HTTP)).thenReturn(null);

        assertEquals(0, gen.generateMetrics(trace, ResourceKey.valueOf("EndPoint", "epName")).size());
    }

    private Frame makeHttpFrame() {
        Operation op = new Operation().type(OperationType.HTTP);
        OperationMap req = op.createMap("request");
        req.put("contentLength", 1000);
        OperationMap res = op.createMap("response");
        res.put("contentSize", 4000l);
        return new SimpleFrame(FrameId.valueOf(1), null, op, timeRange, Collections.<Frame>emptyList());
    }

}
