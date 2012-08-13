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
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.SimpleFrameBuilder;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;
import com.springsource.insight.util.KeyValPair;

public abstract class AbstractRabbitMQResourceAnalyzerTest extends Assert {
	private final RabbitPluginOperationType operationType;
	private final boolean isIncoming;
	private final AbstractRabbitMQResourceAnalyzer analyzer;
	protected static final String	TEST_EXCHANGE="e", TEST_ROUTING_KEY="rk", TEST_HOST="127.0.0.1", TEST_TEMP_ROUTING_KEY="amq.gen-rk77";
	protected static final int	TEST_PORT=5672;

	public AbstractRabbitMQResourceAnalyzerTest(AbstractRabbitMQResourceAnalyzer analyzerInstance) {
		if ((analyzer=analyzerInstance) == null) {
			throw new IllegalStateException("No analyzer instance");
		}
		this.operationType = analyzerInstance.getRabbitPluginOperationType();
		this.isIncoming = analyzerInstance.isIncomingResource();
	}

	@Test
	public void testExchangeLocateEndPoint() {
		Operation op = createOperation();
		KeyValPair<String,String> props = addOperationProps(op, false, true, Boolean.FALSE);
		Trace trace = createValidTrace(op);
		assertEndPointAnalysisResult(trace, props);
	}

	@Test
	public void testExchangeLocateExternalResourceName() {
		Operation op = createOperation();   	
		KeyValPair<String,String> props = addOperationProps(op, false, true, Boolean.FALSE);
		Trace trace = createValidTrace(op);
		assertOneExternalResourceDescriptor(trace, op, props, false);
	}

	@Test
	public void testRoutingKeyLocateEndPoint() {
		Operation op = createOperation();
		KeyValPair<String,String> props = addOperationProps(op, true, false, Boolean.FALSE);
		Trace trace = createValidTrace(op);
		assertEndPointAnalysisResult(trace, props);
	}

	@Test
	public void testRoutingKeyExternalResourceName() {
		Operation op = createOperation();   	
		KeyValPair<String,String> props = addOperationProps(op, true, false, Boolean.FALSE);
		Trace trace = createValidTrace(op);
		assertTwoExternalResourceDescriptors(trace, props, op);
	}

	@Test
	public void testBothLocateEndPoint() {
		Operation op = createOperation();
		KeyValPair<String,String> props = addOperationProps(op, true, true, Boolean.FALSE);
		Trace trace = createValidTrace(op);
		assertEndPointAnalysisResult(trace, props);
	}

	@Test
	public void testBothExternalResourceName() {
		Operation op = createOperation();
		KeyValPair<String,String> props = addOperationProps(op, true, true, Boolean.FALSE);
		Trace trace = createValidTrace(op);
		assertTwoExternalResourceDescriptors(trace, props, op);
	}

	@Test
	public void testExactlyTwoDifferentExternalResourceNames() {   	
		final String	OTHER_HOST="127.0.0.2";
		final int		OTHER_PORT=5673;
		Operation op1 = createOperation(TEST_HOST, TEST_PORT);
		KeyValPair<String,String> props1 = addOperationProps(op1, true, true, Boolean.FALSE);
		Operation op2 = createOperation(OTHER_HOST, OTHER_PORT);
		KeyValPair<String,String> props2 = addOperationProps(op2, true, false, Boolean.FALSE);

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
		assertEquals("Mismatched number of resources", 4, externalResourceDescriptors.size());        
		
		assertParentChildExternalResourceDescriptors(trace, op2, props2, externalResourceDescriptors.subList(0, 2), OTHER_HOST, OTHER_PORT);
		assertParentChildExternalResourceDescriptors(trace, op1, props1, externalResourceDescriptors.subList(2, 4), TEST_HOST, TEST_PORT);
	}
	
	@Test
	public void testExchangeLabel() {
		Operation op = createOperation();
		KeyValPair<String,String> props = addOperationProps(op, false, true, Boolean.FALSE);		
		assertEquals("Mismatched label", "Exchange#e", 
				AbstractRabbitMQResourceAnalyzer.buildLabel(props.getKey(), props.getValue(), true));
	}
	
	@Test
	public void testRoutingKeyLabel() {
		Operation op = createOperation();
		KeyValPair<String,String> props = addOperationProps(op, true, false, Boolean.FALSE);		
		assertEquals("Mismatched label", "Exchange#" + AbstractRabbitMQResourceAnalyzer.NO_EXCHANGE + " RoutingKey#rk", 
				AbstractRabbitMQResourceAnalyzer.buildLabel(props.getKey(), props.getValue(), true));
	}
	
	@Test
	public void testExchangeAndRoutingKeyLabel() {
		Operation op = createOperation();
		KeyValPair<String,String> props = addOperationProps(op, true, true, Boolean.FALSE);		
		assertEquals("Mismatched label", "Exchange#e RoutingKey#rk", 
				AbstractRabbitMQResourceAnalyzer.buildLabel(props.getKey(), props.getValue(), true));
	}
	
	@Test
	public void testExchangeAndTempRoutingKeyLabel() {
		Operation op = createOperation();
		KeyValPair<String,String> props = addOperationProps(op, true, true, Boolean.TRUE);		
		assertEquals("Mismatched label", "Exchange#e RoutingKey#" + AbstractRabbitMQResourceAnalyzer.UNNAMED_TEMP_QUEUE_LABEL, 
				AbstractRabbitMQResourceAnalyzer.buildLabel(props.getKey(), props.getValue(), true));
	}


	/////////////////////
	// Assertion methods
	/////////////////////

	private void assertEndPointAnalysisResult(Trace trace, KeyValPair<String,String> props) {
		EndPointAnalysis	analysis=analyzer.locateEndPoint(trace);
		assertNotNull("No endpoint analysis located", analysis);

		String label = AbstractRabbitMQResourceAnalyzer.buildLabel(props.getKey(), props.getValue(), true);

		assertEquals("Mismatched endpoint name", label, analysis.getEndPointName().getName());
		assertEquals("Mismatched example", analyzer.buildExample(label), analysis.getExample());
		assertEquals("Mismatched resource label", AbstractRabbitMQResourceAnalyzer.buildEndPointLabel(label), analysis.getResourceLabel());
		assertEquals("Mismatched score", AbstractRabbitMQResourceAnalyzer.DEFAULT_SCORE, analysis.getScore());
	}
	
	List<ExternalResourceDescriptor> assertOneExternalResourceDescriptor(Trace trace, Operation op, KeyValPair<String,String> props, boolean useRoutingKey) {
		List<ExternalResourceDescriptor> externalResourceDescriptors=analyzer.locateExternalResourceName(trace);

		assertEquals("Mismatched num. of resources", 1, externalResourceDescriptors.size());

		ExternalResourceDescriptor descriptor = externalResourceDescriptors.get(0);
		assertExternalResourceDescriptorContent(descriptor, props, op, useRoutingKey, TEST_HOST, TEST_PORT, trace);

		return externalResourceDescriptors;
	}
	
	void assertTwoExternalResourceDescriptors(Trace trace, KeyValPair<String,String> props, Operation op) {
		List<ExternalResourceDescriptor> externalResourceDescriptors=analyzer.locateExternalResourceName(trace);
		assertEquals("Mismatched num. of resources", 2, externalResourceDescriptors.size());  
		
		assertParentChildExternalResourceDescriptors(trace, op, props, externalResourceDescriptors, TEST_HOST, TEST_PORT);
	}

	void assertParentChildExternalResourceDescriptors(Trace trace, Operation op, KeyValPair<String,String> props, List<ExternalResourceDescriptor> descriptors, String host, int port) {

		ExternalResourceDescriptor descriptorParent = descriptors.get(0);
		assertExternalResourceDescriptorContent(descriptorParent, props, op, false, host, port, trace);

		ExternalResourceDescriptor descriptorChild = descriptors.get(1);
		assertExternalResourceDescriptorContent(descriptorChild, props, op, true, host, port, trace);

		assertEquals("Mismatched parent", descriptorParent.getName(), descriptorChild.getParent());
	}

	ExternalResourceDescriptor assertExternalResourceDescriptorContent (ExternalResourceDescriptor descriptor,
			KeyValPair<String,String> props, Operation op, boolean useRoutingKey, 
			String host, int port, Trace trace) {
		if (trace != null) {
			assertEquals("Mismatched opeartion frame", op, descriptor.getFrame().getOperation());
		}

		String label = AbstractRabbitMQResourceAnalyzer.buildLabel(props.getKey(), props.getValue(), useRoutingKey);

		assertEquals("Mismatched label", AbstractRabbitMQResourceAnalyzer.buildExternalResourceLabel(label), descriptor.getLabel());
		assertEquals("Mismatched type", ExternalResourceType.QUEUE.name(), descriptor.getType());
		assertEquals("Mismatched vendor", AbstractRabbitMQResourceAnalyzer.RABBIT, descriptor.getVendor());
		assertEquals("Mismatched host", host, descriptor.getHost());
		assertEquals("Mismatched port", port, descriptor.getPort());

		assertEquals("Mismatched hash value", AbstractRabbitMQResourceAnalyzer.buildExternalResourceName(
				props.getKey(),props.getValue(), useRoutingKey, host, port), descriptor.getName());
		assertEquals("Mismatched direction", Boolean.valueOf(isIncoming), Boolean.valueOf(descriptor.isIncoming()));
		return descriptor;
	}

	/////////////////////
	// build stuff
	/////////////////////
	
	protected KeyValPair<String, String> addOperationProps(Operation operation, boolean addRouting, boolean addExchange, Boolean useTempRoutingKey){
		KeyValPair<String, String> props = addInitialOperationProps(operation, addRouting, addExchange, useTempRoutingKey);
		props = setFinalExchangeAndRoutingKey(props);
		return props;
	}
	
	abstract protected KeyValPair<String, String> addInitialOperationProps(Operation operation, boolean addRouting, boolean addExchange, Boolean useTempRoutingKey);

	protected static final KeyValPair<String,String> setRoutingKey (KeyValPair<String,String> org, String key) {
		return new KeyValPair<String, String>((org == null) ? null : org.getKey(), key);
	}

	protected static final KeyValPair<String,String> setExchange (KeyValPair<String,String> org, String exchange) {
		return new KeyValPair<String, String>(exchange, (org == null) ? null : org.getValue());
	}
	
	private static KeyValPair<String,String> setFinalExchangeAndRoutingKey (KeyValPair<String,String> org) {
		return new KeyValPair<String, String>(AbstractRabbitMQResourceAnalyzer.getFinalExchangeName(org.getKey()),
				AbstractRabbitMQResourceAnalyzer.getFinalRoutingKey(org.getValue()));
	}
	
	static Trace createValidTrace(Operation op) {
		SimpleFrameBuilder builder = new SimpleFrameBuilder();

		builder.enter(op);

		Frame frame = builder.exit();
		return Trace.newInstance(ApplicationName.valueOf("app"), TraceId.valueOf("0"), frame);
	}

	Operation createOperation() {
		return createOperation(TEST_HOST, TEST_PORT);
	}

	Operation createOperation (String host, int port) {
		return new Operation()
		.type(operationType.getOperationType())
		.label(operationType.getLabel())
		.putAnyNonEmpty("host", host)
		.put("port", port)
		;
	}
}
