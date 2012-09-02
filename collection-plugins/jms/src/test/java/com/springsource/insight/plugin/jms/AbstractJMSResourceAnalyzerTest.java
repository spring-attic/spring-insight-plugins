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
package com.springsource.insight.plugin.jms;

import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.color.ColorManager;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.server.ServerName;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameId;
import com.springsource.insight.intercept.trace.SimpleFrame;
import com.springsource.insight.intercept.trace.SimpleFrameBuilder;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;
import com.springsource.insight.util.StringUtil;
import com.springsource.insight.util.time.TimeRange;

public abstract class AbstractJMSResourceAnalyzerTest extends Assert {
	private final JMSPluginOperationType operationType;
	private final boolean isIncoming;
	private final AbstractJMSResourceAnalyzer	analyzer;
	/*
	 * NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!!
	 * These values must be the same as the ones used in the JMSServlet class
	 * of the jms-webapp integration test
	 */
	protected static final String	INTTEST_PROTOCOL="vm", INTTEST_HOST="localhost";
	protected static final int	INTTEST_PORT=61618;
	protected static final String	INTTEST_QUEUE_NAME="Dummy ActiveMQ queue", INTTEST_TOPIC_NAME="Dummy Topic";
	protected static final ApplicationName	INTTEST_APP=ApplicationName.valueOf("localhost", "jms_webapp");
	protected static final Map<DestinationType,String>	DESTS_NAMES=Collections.unmodifiableMap(
				new EnumMap<DestinationType,String>(DestinationType.class) {
					private static final long serialVersionUID = 1L;

					{
						put(DestinationType.Queue, INTTEST_QUEUE_NAME);
						put(DestinationType.Topic, INTTEST_TOPIC_NAME);
					}
				}
			);

	protected AbstractJMSResourceAnalyzerTest(AbstractJMSResourceAnalyzer analyzerInstance) {
		if ((this.analyzer=analyzerInstance) == null) {
			throw new IllegalStateException("No analyzer instance provided");
		}

		this.operationType = analyzerInstance.operationType;
		this.isIncoming = analyzerInstance.isIncoming;
	}
	
    @Test
    public void testLocateEndPoint() {
        Trace trace = createValidTrace();        
        EndPointAnalysis analysis = analyzer.locateEndPoint(trace);
        
        assertNotNull("No analysis located", analysis);
        assertEquals("Mismatched example", operationType.getEndPointPrefix() + "Queue#test.queue", analysis.getExample());
        assertEquals("Mismatached endpoint", EndPointName.valueOf("Queue#test.queue"), analysis.getEndPointName());
        assertEquals("Mismatched label", "JMS-Queue#test.queue", analysis.getResourceLabel());
        assertEquals("Mismatched score", AbstractJMSResourceAnalyzer.DEFAULT_SCORE, analysis.getScore());
    }
    
    @Test
	public void testLocateExternalResourceName() {
		Trace trace = createValidTrace();
		List<ExternalResourceDescriptor> externalResourceDescriptors =
				(List<ExternalResourceDescriptor>) analyzer.locateExternalResourceName(trace);

		assertEquals(1, externalResourceDescriptors.size());        
		ExternalResourceDescriptor descriptor = externalResourceDescriptors.get(0);

		assertEquals(trace.getRootFrame(), descriptor.getFrame());
		assertEquals("JMS-Queue#test.queue", descriptor.getLabel());
		assertEquals(ExternalResourceType.QUEUE.name(), descriptor.getType());
		assertEquals("JMS", descriptor.getVendor());
		assertEquals(null, descriptor.getHost());
		assertEquals(-1, descriptor.getPort());
		String expectedHash = MD5NameGenerator.getName("Queue#test.queuenull-1");
		assertEquals("JMS:" + expectedHash, descriptor.getName());
		assertEquals(Boolean.valueOf(isIncoming), Boolean.valueOf(descriptor.isIncoming()));
	}
    
    @Test
	public void testExactlyTwoDifferentExternalResourceNames() {   	
		Operation op1 = createOperation();
		Operation op2 = createOperation();
		op2.put("host", "127.0.0.2");
		op2.put("port", 1111);
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

		List<ExternalResourceDescriptor> externalResourceDescriptors =
				(List<ExternalResourceDescriptor>) analyzer.locateExternalResourceName(trace);

		assertEquals(2, externalResourceDescriptors.size());        

		ExternalResourceDescriptor descriptor = externalResourceDescriptors.get(0);        
		assertEquals(op2, descriptor.getFrame().getOperation());
		assertEquals("JMS-Queue#test.queue", descriptor.getLabel());
		assertEquals(ExternalResourceType.QUEUE.name(), descriptor.getType());
		assertEquals("JMS", descriptor.getVendor());
		assertEquals("127.0.0.2", descriptor.getHost());
		assertEquals(1111, descriptor.getPort());
		String expectedHash = MD5NameGenerator.getName("Queue#test.queue127.0.0.21111");
		assertEquals("JMS:" + expectedHash, descriptor.getName());
		assertEquals(Boolean.valueOf(isIncoming), Boolean.valueOf(descriptor.isIncoming()));

		descriptor = externalResourceDescriptors.get(1);        
		assertEquals(op1, descriptor.getFrame().getOperation());
		assertEquals("JMS-Queue#test.queue", descriptor.getLabel());
		assertEquals(ExternalResourceType.QUEUE.name(), descriptor.getType());
		assertEquals("JMS", descriptor.getVendor());
		assertEquals(null, descriptor.getHost());
		assertEquals(-1, descriptor.getPort());
		expectedHash = MD5NameGenerator.getName("Queue#test.queuenull-1");
		assertEquals("JMS:" + expectedHash, descriptor.getName());
		assertEquals(Boolean.valueOf(isIncoming), Boolean.valueOf(descriptor.isIncoming()));
	}

    /*
     * This isn't a real test as much as it provides useful data for the
     * integration tests in case of failure
     */
    @Test
    public void generateIntegrationTestsHashes () {
    	final Operation	BASE_OP=new Operation()
									.type(operationType.getOperationType())
									.label(getClass().getSimpleName() + "#generateIntegrationTestsHashes")
									.put("host", INTTEST_HOST)
									.put("port", INTTEST_PORT)
									;
    	final Frame	TEST_FRAME=new SimpleFrame(FrameId.valueOf("7365"), null, BASE_OP, new TimeRange(1L, 10L), Collections.<Frame>emptyList());
    	final Trace	TEST_TRACE=new Trace(ServerName.valueOf("7.3.6.5"), INTTEST_APP, new Date(System.currentTimeMillis()), TraceId.valueOf("3777347"), TEST_FRAME);
    	final ColorManager colorManager=ColorManager.getInstance();

    	for (DestinationType destType : DestinationType.values()) {
    		String	destName=DESTS_NAMES.get(destType);
    		if (StringUtil.isEmpty(destName)) {
    			continue;
    		}

    		Operation	op=AbstractJMSCollectionAspect.applyDestinationData(BASE_OP, destType, destName);
    		assertSame("Mismatched test operation instance for " + destType + "[" + destName + "]", BASE_OP, op);

    		ExternalResourceDescriptor	desc=analyzer.createExternalResourceDescriptor(colorManager, TEST_FRAME);
    		assertNotNull("No descriptor for " + destType + "[" + destName + "]", desc);
    		System.out.append('\t').append(destType.name()).append('[').append(destName).append("]: ").println(desc);

    		EndPointAnalysis	analysis=analyzer.locateEndPoint(TEST_TRACE);
    		assertNotNull("No analysis for " + destType + "[" + destName + "]", analysis);
    	}
    }

    private Trace createValidTrace() {
        SimpleFrameBuilder builder = new SimpleFrameBuilder();
        Operation op = createOperation();
        
        builder.enter(op);
        
        Frame frame = builder.exit();
        return Trace.newInstance(ApplicationName.valueOf("app"), TraceId.valueOf("0"), frame);
    }

	private Operation createOperation() {
		Operation op = new Operation().type(operationType.getOperationType());
        
        op.put("destinationName", "test.queue");
        op.put("destinationType", "Queue");
        op.put(OperationFields.METHOD_SIGNATURE, "test.queue");
        op.put(OperationFields.CLASS_NAME, "Queue");
		return op;
	}

}
