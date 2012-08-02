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
package com.springsource.insight.plugin.apache.http.hc4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.springsource.insight.intercept.metrics.AbstractMetricsGenerator;
import com.springsource.insight.intercept.metrics.MetricsBag;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.resource.ResourceKey;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameId;
import com.springsource.insight.intercept.trace.SimpleFrame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.IDataPoint;
import com.springsource.insight.util.time.TimeRange;


public class HttpMetricsGeneratorTest {

    private TimeRange timeRange = new TimeRange(1304387418963003000l, 1304387419123224000l);


    @Test
    public void generateMetrics() {
        Trace trace = mock(Trace.class);
        when(trace.getAllFramesOfType(HttpClientDefinitions.TYPE)).thenReturn(makeHttpClientFrames());
        when(trace.getRange()).thenReturn(timeRange);
        
        ResourceKey endPointName = ResourceKey.valueOf("EndPoint", "epName");

        HttpClientMetricsGenerator gen = new HttpClientMetricsGenerator();
        List<MetricsBag> mbs = gen.generateMetrics(trace, endPointName);
        assertTrue(mbs.size() == 1);
        
        MetricsBag mb = mbs.get(0);

        List<String> keys = mb.getMetricKeys();
        assertEquals(2, keys.size());
        
        assertEquals("opExtKey", mb.getResourceKey().getName());

        List<IDataPoint> points = mb.getPoints(AbstractMetricsGenerator.EXECUTION_TIME);
        assertEquals(1, points.size());
        assertEquals(160.0 , points.get(0).getValue(), 0.01);

        points = mb.getPoints(AbstractMetricsGenerator.INVOCATION_COUNT);
        assertEquals(1, points.size());
        assertEquals(1.0, points.get(0).getValue(), 0.0);
    }

    @Test
    public void noHttpClientFrame() {
        Trace trace = mock(Trace.class);        
        when(trace.getFirstFrameOfType(HttpClientDefinitions.TYPE)).thenReturn(null);
        
        ResourceKey endPointName = mock(ResourceKey.class);
        HttpClientMetricsGenerator gen = new HttpClientMetricsGenerator();
        assertEquals(0, gen.generateMetrics(trace, endPointName).size());
    }
    
    
    private List<Frame> makeHttpClientFrames() {
        Operation op = new Operation().type(HttpClientDefinitions.TYPE);
        op.put(ResourceKey.OPERATION_KEY, "insight:name=\"opExtKey\",type=EndPoint");
        List<Frame> res = new ArrayList<Frame>();
        res.add(new SimpleFrame(FrameId.valueOf(1), null, op, timeRange, Collections.<Frame>emptyList()));
        return res;
    }


}
