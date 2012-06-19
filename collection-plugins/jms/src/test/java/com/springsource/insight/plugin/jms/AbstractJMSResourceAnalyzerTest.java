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
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.SimpleFrameBuilder;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;

public abstract class AbstractJMSResourceAnalyzerTest {

	protected abstract AbstractJMSResourceAnalyzer getAnalyzer();
	private final JMSPluginOperationType operationType;

	protected AbstractJMSResourceAnalyzerTest(JMSPluginOperationType type) {
		this.operationType = type;
	}
	
    @Test
    public void testLocateEndPoint() {
        AbstractJMSResourceAnalyzer analyzer = getAnalyzer();        
        Trace trace = createValidTrace();        
        EndPointAnalysis analysis = analyzer.locateEndPoint(trace);
        
        assertNotNull(analysis);
        assertEquals(operationType.getEndPointPrefix() + "Queue#test.queue", analysis.getExample());
        assertEquals(EndPointName.valueOf("Queue#test.queue"), analysis.getEndPointName());
        assertEquals("JMS-Queue#test.queue", analysis.getResourceLabel());
        assertEquals(1, analysis.getScore());
    }
    
    @Test
	public void testLocateExternalResourceName() {
    	AbstractJMSResourceAnalyzer analyzer = getAnalyzer();
		Trace trace = createValidTrace();
		List<ExternalResourceDescriptor> externalResourceDescriptors = analyzer.locateExternalResourceName(trace);

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

		AbstractJMSResourceAnalyzer analyzer = getAnalyzer();
		List<ExternalResourceDescriptor> externalResourceDescriptors = analyzer.locateExternalResourceName(trace);

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

		descriptor = externalResourceDescriptors.get(1);        
		assertEquals(op1, descriptor.getFrame().getOperation());
		assertEquals("JMS-Queue#test.queue", descriptor.getLabel());
		assertEquals(ExternalResourceType.QUEUE.name(), descriptor.getType());
		assertEquals("JMS", descriptor.getVendor());
		assertEquals(null, descriptor.getHost());
		assertEquals(-1, descriptor.getPort());
		expectedHash = MD5NameGenerator.getName("Queue#test.queuenull-1");
		assertEquals("JMS:" + expectedHash, descriptor.getName());
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
