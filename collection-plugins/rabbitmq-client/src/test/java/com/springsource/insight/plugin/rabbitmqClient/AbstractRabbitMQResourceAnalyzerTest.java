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
import com.springsource.insight.intercept.endpoint.EndPointName;
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
		KeyValPair<String,String>	props=addOperationProps(op, false, true, false);
		Trace trace = createValidTrace(op);
		assertAnalysisResult(trace, createLabel(props));
	}

	@Test
	public void testExchangeLocateExternalResourceName() {
		Operation op = createOperation();   	
		KeyValPair<String,String>	props=addOperationProps(op, false, true, false);
		Trace trace = createValidTrace(op);
		assertExternalResourceDescriptors(trace, props);
	}

	@Test
	public void testRoutingKeyLocateEndPoint() {
		Operation op = createOperation();
		KeyValPair<String,String>	props=addOperationProps(op, true, false, false);
		Trace trace = createValidTrace(op);
		assertAnalysisResult(trace, createLabel(props));
	}
	
	@Test
	public void testTempRoutingKeyLocateEndPoint() {
		Operation op = createOperation();
		KeyValPair<String,String>	props=addOperationProps(op, true, false, true);
		Trace trace = createValidTrace(op);
		assertAnalysisResult(trace, "Exchange#DefaultExchange RoutingKey#temporaryQueue");
	}

	@Test
	public void testRoutingKeyExternalResourceName() {
		Operation op = createOperation();   	
		KeyValPair<String,String>	props=addOperationProps(op, true, false, false);
		Trace trace = createValidTrace(op);
		assertExternalResourceDescriptors(trace, props);
	}

	@Test
	public void testBothLocateEndPoint() {
		Operation op = createOperation();
		KeyValPair<String,String>	props=addOperationProps(op, true, true, false);
		Trace trace = createValidTrace(op);
		assertAnalysisResult(trace, createLabel(props));
	}

	@Test
	public void testBothExternalResourceName() {
		Operation op = createOperation();
		KeyValPair<String,String>	props=addOperationProps(op, true, true, false);
		Trace trace = createValidTrace(op);
		assertExternalResourceDescriptors(trace, props);
	}

	@Test
	public void testExactlyTwoDifferentExternalResourceNames() {   	
		final String	OTHER_HOST="127.0.0.2";
		final int		OTHER_PORT=5673;
		Operation op1 = createOperation(TEST_HOST, TEST_PORT);
		KeyValPair<String,String>	props1=addOperationProps(op1, true, true, false);
		Operation op2 = createOperation(OTHER_HOST, OTHER_PORT);
		KeyValPair<String,String>	props2=addOperationProps(op2, true, false, false);

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
		assertEquals("Mismatched number of resources", 2, externalResourceDescriptors.size());        
		ExternalResourceDescriptor	desc2=
				assertExternalResourceDescriptorContent(externalResourceDescriptors.get(0), createLabel(props2), OTHER_HOST, OTHER_PORT, null);
		assertSame("Mismatched 2nd frame operation", op2, desc2.getFrame().getOperation());

		ExternalResourceDescriptor	desc1 =
				assertExternalResourceDescriptorContent(externalResourceDescriptors.get(1), createLabel(props1), TEST_HOST, TEST_PORT, null);
		assertSame("Mismatched 1st frame operation", op1, desc1.getFrame().getOperation());
	}

	// returns exchange + routing key
	abstract protected KeyValPair<String,String> addOperationProps(Operation operation, boolean addRouting, boolean addExchange, Boolean useTempRoutingKey);

	protected static final KeyValPair<String,String> setExchange (KeyValPair<String,String> org, String exchange) {
		return new KeyValPair<String, String>(exchange, (org == null) ? null : org.getValue());
	}

	protected static final KeyValPair<String,String> setRoutingKey (KeyValPair<String,String> org, String key) {
		return new KeyValPair<String, String>((org == null) ? null : org.getKey(), key);
	}

	static Trace createValidTrace(Operation op) {
		SimpleFrameBuilder builder = new SimpleFrameBuilder();

		builder.enter(op);

		Frame frame = builder.exit();
		return Trace.newInstance(ApplicationName.valueOf("app"), TraceId.valueOf("0"), frame);
	}

	List<ExternalResourceDescriptor> assertExternalResourceDescriptors(Trace trace, KeyValPair<String,String> props) {
		return assertExternalResourceDescriptors(trace, createLabel(props));
	}

	private void assertAnalysisResult(Trace trace, String label) {
		EndPointAnalysis	analysis=analyzer.locateEndPoint(trace);
		assertNotNull("No endpoint analysis located", analysis);

		EndPointName	endpoint=analysis.getEndPointName();		
		String			resLabel=AbstractRabbitMQResourceAnalyzer.buildResourceLabel(label);

		assertEquals("Mismatched endpoint name", label, endpoint.getName());
		assertEquals("Mismatched example", AbstractRabbitMQResourceAnalyzer.buildDefaultExample(operationType, label), analysis.getExample());
		assertEquals("Mismatched resource label", resLabel, analysis.getResourceLabel());
		assertEquals("Mismatched score", AbstractRabbitMQResourceAnalyzer.DEFAULT_SCORE, analysis.getScore());
	}

	List<ExternalResourceDescriptor> assertExternalResourceDescriptors (Trace trace, String label) {
		List<ExternalResourceDescriptor> externalResourceDescriptors=analyzer.locateExternalResourceName(trace);
		assertExternalResourceDescriptors(externalResourceDescriptors, label, trace);
		return externalResourceDescriptors;
	}

	ExternalResourceDescriptor assertExternalResourceDescriptors(List<ExternalResourceDescriptor> externalResourceDescriptors, String label, Trace trace){
		assertEquals("Mismatched num. of resources", 1, externalResourceDescriptors.size());        
		ExternalResourceDescriptor descriptor = externalResourceDescriptors.get(0);
		assertExternalResourceDescriptorContent(descriptor, label, TEST_HOST, TEST_PORT, trace);
		return descriptor;
	}

	ExternalResourceDescriptor assertExternalResourceDescriptorContent (ExternalResourceDescriptor descriptor,
												  String label, String host, int port,
												  Trace trace) {
		if (trace != null) {
			assertEquals("Mismatched root frame", trace.getRootFrame(), descriptor.getFrame());
		}

		assertEquals("Mismatched label", AbstractRabbitMQResourceAnalyzer.buildResourceLabel(label), descriptor.getLabel());
		assertEquals("Mismatched type", ExternalResourceType.QUEUE.name(), descriptor.getType());
		assertEquals("Mismatched vendor", AbstractRabbitMQResourceAnalyzer.RABBIT, descriptor.getVendor());
		assertEquals("Mismatched host", host, descriptor.getHost());
		assertEquals("Mismatched port", port, descriptor.getPort());

		assertEquals("Mismatched hash value", AbstractRabbitMQResourceAnalyzer.buildResourceName(label, host, port), descriptor.getName());
		assertEquals("Mismatched direction", Boolean.valueOf(isIncoming), Boolean.valueOf(descriptor.isIncoming()));
		return descriptor;
	}

	private static String createLabel (KeyValPair<String,String> props) {
		return AbstractRabbitMQResourceAnalyzer.buildDefaultLabel(
				(props == null) ? null : props.getKey(),
				(props == null) ? null : props.getValue());
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
