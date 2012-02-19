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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.springsource.insight.intercept.metrics.MetricsBag;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.resource.ResourceKey;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameId;
import com.springsource.insight.intercept.trace.SimpleFrame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.IDataPoint;
import com.springsource.insight.util.time.TimeRange;

public abstract class AbstractRabbitMQMetricsGeneratorTest {

    private TimeRange timeRange = new TimeRange(1304387418963003000l, 1304387419123224000l);

    private RabbitPluginOperationType operationType;
    
    public AbstractRabbitMQMetricsGeneratorTest(RabbitPluginOperationType operationType) {
		super();
		this.operationType = operationType;
	}

    protected abstract AbstractRabbitMetricsGenerator createRabbitMetricsGenerator();
    
    @Test
    public final void testGenerateMetrics() {
        Trace trace = mock(Trace.class);
        when(trace.getLastFramesOfType(operationType.getOperationType())).thenReturn(createRabbitFrames());
        when(trace.getRange()).thenReturn(timeRange);
        
        AbstractRabbitMetricsGenerator gen = createRabbitMetricsGenerator();
        List<MetricsBag> mbs = gen.generateMetrics(trace, ResourceKey.valueOf("EndPoint", "epName"));
        assertEquals(3, mbs.size());
        
        assertExternalResourceMetricBag(gen, mbs.get(0));
        assertExternalResourceMetricBag(gen, mbs.get(1));
        
		List<String> keys;
		List<IDataPoint> points;
        MetricsBag mb = mbs.get(2);
        assertEquals("epName", mb.getResourceKey().getName());
        keys = mb.getMetricKeys();
        assertEquals(1, keys.size());
        
        assertTrue(keys.get(0).equals(gen.createMetricKey()));        
        points = mb.getPoints(gen.createMetricKey());
        assertEquals(1, points.size());
        assertEquals(2d , points.get(0).getValue(), 0);
    }

	private void assertExternalResourceMetricBag(
			AbstractRabbitMetricsGenerator gen, MetricsBag mb) {
		assertEquals("opExtKey", mb.getResourceKey().getName());
        List<String> keys = mb.getMetricKeys();
        assertEquals(3, keys.size());
        
        assertTrue(keys.get(0).equals(AbstractRabbitMetricsGenerator.EXECUTION_TIME));
        List<IDataPoint> points = mb.getPoints(AbstractRabbitMetricsGenerator.EXECUTION_TIME);
        assertEquals(1, points.size());
        assertEquals(160.0 , points.get(0).getValue(), 0.01);
        
        assertTrue(keys.get(1).equals(AbstractRabbitMetricsGenerator.INVOCATION_COUNT));
        points = mb.getPoints(AbstractRabbitMetricsGenerator.INVOCATION_COUNT);
        assertEquals(1, points.size());
        assertEquals(1.0 , points.get(0).getValue(), 0.01);
        
        assertTrue(keys.get(2).equals(AbstractRabbitMetricsGenerator.EXTERNAL_METRIC_PREFIX + gen.createMetricKey()));        
        points = mb.getPoints(AbstractRabbitMetricsGenerator.EXTERNAL_METRIC_PREFIX + gen.createMetricKey());
        assertEquals(1, points.size());
        assertEquals(1d, points.get(0).getValue(), 0);
	}

    @Test
    public final void noRabbitFrame() {
        Trace trace = mock(Trace.class);
        when(trace.getFirstFrameOfType(operationType.getOperationType())).thenReturn(null);
        
        assertEquals(0, createRabbitMetricsGenerator().generateMetrics(trace,  ResourceKey.valueOf("EndPoint", "epName")).size());
    }

    private List<Frame> createRabbitFrames() {
        Operation op = new Operation().type(operationType.getOperationType());
        op.put(ResourceKey.OPERATION_KEY, "insight:name=\"opExtKey\",type=EndPoint");
        
        List<Frame> res = new ArrayList<Frame>();
        SimpleFrame simpleFrame1 = new SimpleFrame(new FrameId(1), null, op, timeRange, Collections.<Frame>emptyList());
		res.add(simpleFrame1);
        SimpleFrame simpleFrame2 = new SimpleFrame(new FrameId(2), null, op, timeRange, Collections.<Frame>emptyList());
		res.add(simpleFrame2);
		
        return res;
    }

}