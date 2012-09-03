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
package com.springsource.insight.plugin.mail;

import java.util.List;

import org.junit.Test;

import com.springsource.insight.collection.test.AbstractCollectionTestSupport;
import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.SimpleFrameBuilder;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;

public class MailResourceAnalyzerTest extends AbstractCollectionTestSupport  {
    private static final String PROTOCOL="POP3";
    private static final Integer    PORT=MailDefinitions.protocolToPortMap.get(PROTOCOL);
    private final MailResourceAnalyzer analyzer = new MailResourceAnalyzer();

    @Test
	public void testLocateExternalResourceName() {
        final String HOST="test.example.com";
		Trace trace = createValidTrace(HOST);
		List<ExternalResourceDescriptor> externalResourceDescriptors =
				(List<ExternalResourceDescriptor>) analyzer.locateExternalResourceName(trace);

		assertEquals("Mismatched number of descriptors", 1, externalResourceDescriptors.size());        
		ExternalResourceDescriptor descriptor = externalResourceDescriptors.get(0);

		assertSame("Mismatched root frame", trace.getRootFrame(), descriptor.getFrame());
		assertDescriptorContents("testLocateExternalResourceName", HOST, descriptor);
	}
    
    @Test
	public void testExactlyTwoDifferentExternalResourceNames() {
        final String    HOST1="op1.example.com", HOST2="op2.example.com";
		Operation op1 = createOperation(HOST1);
		Operation op2 = createOperation(HOST2);

		SimpleFrameBuilder builder = new SimpleFrameBuilder();
		builder.enter(new Operation().type(OperationType.HTTP));
		builder.enter(op2);
		builder.exit();
		builder.enter(new Operation().type(OperationType.METHOD));
		builder.enter(op1);
		builder.exit();
		builder.exit();
		Frame frame = builder.exit();
		Trace trace = Trace.newInstance(ApplicationName.valueOf("app"), TraceId.valueOf("0"), frame);

		List<ExternalResourceDescriptor> externalResourceDescriptors =
				(List<ExternalResourceDescriptor>) analyzer.locateExternalResourceName(trace);
		assertEquals("Mismatched number of descriptors", 2, externalResourceDescriptors.size());        

		ExternalResourceDescriptor descriptor = externalResourceDescriptors.get(0);  
		assertSame("Mismatched 2nd operation instance", op2, descriptor.getFrame().getOperation());
		assertDescriptorContents(HOST2, HOST2, descriptor);
		
		descriptor = externalResourceDescriptors.get(1);
		assertSame("Mismatched 1st operation instance", op1, descriptor.getFrame().getOperation());
        assertDescriptorContents(HOST1, HOST1, descriptor);
	}

    private ExternalResourceDescriptor assertDescriptorContents (
            String testName, String host, ExternalResourceDescriptor descriptor) {
        String  label=PROTOCOL + ":" + host + ":" + PORT;
        assertNotNull(testName + ": No descriptor", descriptor);
        assertEquals(testName + ": Mismatched label", label, descriptor.getLabel());
        assertEquals(testName + ": Mismatched type", MailResourceAnalyzer.RESOURCE_TYPE, descriptor.getType());
        assertEquals(testName + ": Mismatched vendor", PROTOCOL, descriptor.getVendor());
        assertEquals(testName + ": Mismatched host", host, descriptor.getHost());
        assertEquals(testName + ": Mismatched port", PORT.intValue(), descriptor.getPort());
        assertEquals(testName + ": Mismatched direction", Boolean.FALSE, Boolean.valueOf(descriptor.isIncoming()));
        

        String expectedHash = MD5NameGenerator.getName(label);
        assertEquals(testName + ": Mismatched name", PROTOCOL + ":" + expectedHash, descriptor.getName());
        return descriptor;
    }

    private Trace createValidTrace(String host) {
        SimpleFrameBuilder builder = new SimpleFrameBuilder();
        Operation op = createOperation(host);
        
        builder.enter(op);
        
        Frame frame = builder.exit();
        return Trace.newInstance(ApplicationName.valueOf("app"), TraceId.valueOf("0"), frame);
    }

	private Operation createOperation(String host) {
		return new Operation()
		        .type(MailDefinitions.SEND_OPERATION)
		        .put(MailDefinitions.SEND_HOST, host)
		        .put(MailDefinitions.SEND_PORT, PORT.intValue())
		        .put(MailDefinitions.SEND_PROTOCOL, PROTOCOL)
		        ;
	}

}
