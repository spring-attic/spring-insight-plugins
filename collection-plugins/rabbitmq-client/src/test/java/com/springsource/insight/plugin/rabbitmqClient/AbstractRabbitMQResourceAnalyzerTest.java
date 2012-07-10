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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.SimpleFrameBuilder;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;

public abstract class AbstractRabbitMQResourceAnalyzerTest extends Assert {
	private final RabbitPluginOperationType operationType;
	private final boolean isIncoming;
	private final AbstractRabbitMQResourceAnalyzer analyzer;

	public AbstractRabbitMQResourceAnalyzerTest(AbstractRabbitMQResourceAnalyzer analyzerInstance) {
		if ((analyzer=analyzerInstance) == null) {
			throw new IllegalStateException("No analyzer instance");
		}
		this.operationType = analyzerInstance.getRabbitPluginOperationType();
		this.isIncoming = analyzerInstance.isIncomingResource();
	}

	protected void addOperationProps(Operation operation, boolean addRouting, boolean addExchange){
		operation.putAnyNonEmpty("host", "127.0.0.1");
		operation.put("port", 5672);
	}

	@Test
	public void testExchangeLocateEndPoint() {
		Operation op = createOperation();
		addOperationProps(op, false, true);
		Trace trace = createValidTrace(op);

		EndPointAnalysis analysis = analyzer.locateEndPoint(trace);
		String name = analysis.getEndPointName().getName();
		String example = analysis.getExample();
		String lbl = analysis.getResourceLabel();
		int score = analysis.getScore();

		assertEquals("Exchange#e", name);
		assertEquals(operationType.getEndPointPrefix()+"Exchange#e", example);
		assertEquals("RabbitMQ-Exchange#e", lbl);
		assertEquals(1, score);
	}

	@Test
	public void testExchangeLocateExternalResourceName() {
		Operation op = createOperation();   	
		addOperationProps(op, false, true);
		Trace trace = createValidTrace(op);

		List<ExternalResourceDescriptor> externalResourceDescriptors = analyzer.locateExternalResourceName(trace);
		assertExternalResourceDescriptors(externalResourceDescriptors, "Exchange#e", trace, "Exchange#e127.0.0.15672" + isIncoming);
	}

	@Test
	public void testRoutingKeyLocateEndPoint() {
		Operation op = createOperation();
		addOperationProps(op, true, false);
		Trace trace = createValidTrace(op);

		EndPointAnalysis analysis = analyzer.locateEndPoint(trace);
		String name = analysis.getEndPointName().getName();
		String example = analysis.getExample();
		String lbl = analysis.getResourceLabel();
		int score = analysis.getScore();

		assertEquals("RoutingKey#rk", name);
		assertEquals(operationType.getEndPointPrefix()+"RoutingKey#rk", example);
		assertEquals("RabbitMQ-RoutingKey#rk", lbl);
		assertEquals(1, score);
	}

	@Test
	public void testRoutingKeyExternalResourceName() {
		Operation op = createOperation();   	
		addOperationProps(op, true, false);
		Trace trace = createValidTrace(op);

		List<ExternalResourceDescriptor> externalResourceDescriptors = analyzer.locateExternalResourceName(trace);
		assertExternalResourceDescriptors(externalResourceDescriptors, "RoutingKey#rk", trace, "RoutingKey#rk127.0.0.15672" + isIncoming);
	}

	@Test
	public void testBothLocateEndPoint() {
		Operation op = createOperation();
		addOperationProps(op, true, true);
		Trace trace = createValidTrace(op);

		EndPointAnalysis analysis = analyzer.locateEndPoint(trace);

		String name = analysis.getEndPointName().getName();
		String example = analysis.getExample();
		String lbl = analysis.getResourceLabel();
		int score = analysis.getScore();

		assertEquals("Exchange#e RoutingKey#rk", name);
		assertEquals(operationType.getEndPointPrefix()+"Exchange#e RoutingKey#rk", example);
		assertEquals("RabbitMQ-Exchange#e RoutingKey#rk", lbl);
		assertEquals(1, score);
	}

	@Test
	public void testBothExternalResourceName() {
		Operation op = createOperation();
		addOperationProps(op, true, true);
		Trace trace = createValidTrace(op);

		List<ExternalResourceDescriptor> externalResourceDescriptors = analyzer.locateExternalResourceName(trace);

		assertExternalResourceDescriptors(externalResourceDescriptors, "Exchange#e RoutingKey#rk", trace, "Exchange#e RoutingKey#rk127.0.0.15672" + isIncoming);
	}

	@Test
	public void testExactlyTwoDifferentExternalResourceNames() {   	
		Operation op1 = createOperation();
		addOperationProps(op1, true, true);
		Operation op2 = createOperation();
		addOperationProps(op2, true, false);
		op2.put("host", "127.0.0.2");
		op2.put("port", 5673);
		Operation dummyOp = new Operation();

		SimpleFrameBuilder builder = new SimpleFrameBuilder();
		builder.enter(new Operation().type(OperationType.HTTP));
		builder.enter(op2);
		builder.exit();
		builder.enter(dummyOp);
		builder.enter(op1);
		builder.exit();
		builder.exit();
		Frame frame = builder.exit();
		Trace trace = Trace.newInstance(ApplicationName.valueOf("app"), TraceId.valueOf("0"), frame);

		List<ExternalResourceDescriptor> externalResourceDescriptors = analyzer.locateExternalResourceName(trace);

		assertEquals(2, externalResourceDescriptors.size());        

		ExternalResourceDescriptor descriptor = externalResourceDescriptors.get(0);        
		assertEquals(op2, descriptor.getFrame().getOperation());
		assertEquals("RabbitMQ-" + "RoutingKey#rk", descriptor.getLabel());
		assertEquals(ExternalResourceType.QUEUE.name(), descriptor.getType());
		assertEquals("RabbitMQ", descriptor.getVendor());
		assertEquals("127.0.0.2", descriptor.getHost());
		assertEquals(5673, descriptor.getPort());
		String expectedHash = MD5NameGenerator.getName("RoutingKey#rk127.0.0.25673" + isIncoming);
		assertEquals("RabbitMQ:" + expectedHash, descriptor.getName());

		descriptor = externalResourceDescriptors.get(1);        
		assertEquals(op1, descriptor.getFrame().getOperation());
		assertEquals("RabbitMQ-" + "Exchange#e RoutingKey#rk", descriptor.getLabel());
		assertEquals(ExternalResourceType.QUEUE.name(), descriptor.getType());
		assertEquals("RabbitMQ", descriptor.getVendor());
		assertEquals("127.0.0.1", descriptor.getHost());
		assertEquals(5672, descriptor.getPort());
		expectedHash = MD5NameGenerator.getName("Exchange#e RoutingKey#rk127.0.0.15672" + isIncoming);
		assertEquals("RabbitMQ:" + expectedHash, descriptor.getName());
	}

	static Trace createValidTrace(Operation op) {
		SimpleFrameBuilder builder = new SimpleFrameBuilder();

		builder.enter(op);

		Frame frame = builder.exit();
		return Trace.newInstance(ApplicationName.valueOf("app"), TraceId.valueOf("0"), frame);
	}

	void assertExternalResourceDescriptors(List<ExternalResourceDescriptor> externalResourceDescriptors, String label, Trace trace, String stringTohash){
		assertEquals(1, externalResourceDescriptors.size());        
		ExternalResourceDescriptor descriptor = externalResourceDescriptors.get(0);

		assertEquals(trace.getRootFrame(), descriptor.getFrame());
		assertEquals("RabbitMQ-" + label, descriptor.getLabel());
		assertEquals(ExternalResourceType.QUEUE.name(), descriptor.getType());
		assertEquals("RabbitMQ", descriptor.getVendor());
		assertEquals("127.0.0.1", descriptor.getHost());
		assertEquals(5672, descriptor.getPort());
		String expectedHash = MD5NameGenerator.getName(stringTohash);
		assertEquals("RabbitMQ:" + expectedHash, descriptor.getName());
		assertEquals(Boolean.valueOf(isIncoming), Boolean.valueOf(descriptor.isIncoming()));
	}

	Operation createOperation() {
		return new Operation()
					.type(operationType.getOperationType())
					.label(operationType.getLabel())
					;
	}
}
