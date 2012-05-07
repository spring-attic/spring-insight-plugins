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

package com.springsource.insight.plugin.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.springsource.insight.intercept.metrics.AbstractMetricsGenerator;
import com.springsource.insight.intercept.metrics.AbstractMetricsGeneratorTest;
import com.springsource.insight.intercept.metrics.MetricsBag;
import com.springsource.insight.intercept.metrics.MetricsGenerator;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.util.IDataPoint;

public abstract class AbstractJMSMetricsGeneratorTest extends AbstractMetricsGeneratorTest {

	private JMSPluginOperationType operationType;

	public AbstractJMSMetricsGeneratorTest(JMSPluginOperationType opType) {
		this.operationType = opType;
	}    

	@Override
	protected OperationType getOperationType(){
		return operationType.getOperationType();
	}
    
	@Override
	protected void validateMetricsBags(List<MetricsBag> mbs, MetricsGenerator gen) {
		AbstractJMSMetricsGenerator gen1 = (AbstractJMSMetricsGenerator)gen;
		assertEquals(3, mbs.size());
		assertExternalResourceMetricBag(gen1, mbs.get(0));
		assertExternalResourceMetricBag(gen1, mbs.get(1));

		List<String> keys;
		List<IDataPoint> points;
		MetricsBag mb = mbs.get(2);
		assertEquals("epName", mb.getResourceKey().getName());
		keys = mb.getMetricKeys();
		assertEquals(1, keys.size());

		assertTrue(keys.get(0).equals(gen1.createMetricKey()));        
		points = mb.getPoints(gen1.createMetricKey());
		assertEquals(1, points.size());
		assertEquals(2d , points.get(0).getValue(), 0);
	}

	private void assertExternalResourceMetricBag(
			AbstractJMSMetricsGenerator gen, MetricsBag mb) {
		assertEquals("opExtKey", mb.getResourceKey().getName());
        List<String> keys = mb.getMetricKeys();
        assertEquals(3, keys.size());
        
        assertTrue(keys.get(0).equals(AbstractMetricsGenerator.EXECUTION_TIME));
        List<IDataPoint> points = mb.getPoints(AbstractMetricsGenerator.EXECUTION_TIME);
        assertEquals(1, points.size());
        assertEquals(160.0 , points.get(0).getValue(), 0.01);
        
        assertTrue(keys.get(1).equals(AbstractMetricsGenerator.INVOCATION_COUNT));
        points = mb.getPoints(AbstractMetricsGenerator.INVOCATION_COUNT);
        assertEquals(1, points.size());
        assertEquals(1.0 , points.get(0).getValue(), 0.01);
        
        assertTrue(keys.get(2).equals(gen.createMetricKey()));        
        points = mb.getPoints(gen.createMetricKey());
        assertEquals(1, points.size());
        assertEquals(1d, points.get(0).getValue(), 0);
	}
}