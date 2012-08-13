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
package com.springsource.insight.plugin.jdbc;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

import org.junit.Test;

import com.springsource.insight.intercept.metrics.AbstractMetricsGeneratorTest;
import com.springsource.insight.intercept.metrics.MetricsBag;
import com.springsource.insight.intercept.metrics.MetricsGenerator;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.resource.ResourceKey;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.ListUtil;


public class JdbcMetricsGeneratorTest extends AbstractMetricsGeneratorTest {
	public JdbcMetricsGeneratorTest () {
		super();
	}

	@Test
	public void testJdbcOperationMetricsGeneratorTraceTestAnalysis () throws Exception {
		Trace		trace=null;
		Thread		thread=Thread.currentThread();
		ClassLoader	cl=thread.getContextClassLoader();
		InputStream	inStream=cl.getResourceAsStream("JdbcTraceTest.ser");
		assertNotNull("Not found serialized trace resource", inStream);

		try {
			ObjectInputStream	objIn=new ObjectInputStream(inStream);
			try {
				trace = (Trace) objIn.readObject();
			} finally {
				objIn.close();
			}
		} finally {
			inStream.close();
		}

		JdbcOperationMetricsGenerator	generator=new JdbcOperationMetricsGenerator();
		ResourceKey						defaultKey=ResourceKey.valueOf("insight:Application=\"localhost|bounce_hsqlin\",Server=\"173b5530-7fe8-4375-a576-240e19aea814\",name=\"com.springsource.insight.tests.bounce_hsqlin.BouncerController#list(Integer,Integer,ModelMap)\",type=Application.Server.EndPoint");
		List<MetricsBag>				metrics=generator.generateMetrics(trace, defaultKey);
		assertEquals("Mismatched metrics size: " + metrics, 2, ListUtil.size(metrics));
		for (MetricsBag mb : metrics) {
			assertDefaultExternalResourceMetricsFound(mb);
		}
	}

	@Override
	protected MetricsGenerator getMetricsGenerator() {
		return new TestingGenerator();
	}

	@Override
	protected OperationType getOperationType() {
		return TestingGenerator.TYPE;
	}

	static class TestingGenerator extends JdbcMetricsGenerator {
	    static final OperationType TYPE=OperationType.valueOf("test-gen");
	    TestingGenerator () {
	        super(TYPE);
	    }
	}
}