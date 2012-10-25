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

package com.springsource.insight.plugin.integration;

import java.util.Collections;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.integration.ip.tcp.connection.ConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpConnection;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameId;
import com.springsource.insight.util.time.TimeRange;

/**
 * 
 */
public class TcpConnectionOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	private static final TcpConnectionExternalResourceAnalyzer	analyzer=TcpConnectionExternalResourceAnalyzer.getInstance();

	public TcpConnectionOperationCollectionAspectTest() {
		super();
	}

	@Test
	public void testConnectionCreationCollected () throws Exception {
		Operation	op=assertConnectionOperation((ConnectionFactory) new TcpTestConnectionFactory("localhost", 7365, false));
		assertExternalResourceAnalysis(op);
	}

	private ExternalResourceDescriptor assertExternalResourceAnalysis (Operation op) {
        Frame	frame=Mockito.mock(Frame.class);
        Mockito.when(frame.getId()).thenReturn(FrameId.valueOf(7365L));
        Mockito.when(frame.getOperation()).thenReturn(op);
        Mockito.when(frame.getChildren()).thenReturn(Collections.<Frame>emptyList());
        Mockito.when(frame.getParent()).thenReturn(null);
        Mockito.when(frame.getRange()).thenReturn(new TimeRange(7365L, 3777347L));
        
        ExternalResourceDescriptor	desc=analyzer.extractExternalResourceDescriptor(frame);
        assertNotNull("No resource", desc);
        assertSame("Mismatched frame", frame, desc.getFrame());
        assertEquals("Mismatched host", op.get(TcpConnectionOperationCollector.HOST_ADDRESS_ATTR, String.class), desc.getHost());
        assertEquals("Mismatched port", op.get(TcpConnectionOperationCollector.PORT_ATTR, Number.class).intValue(), desc.getPort());
        assertEquals("Mismatched type", ExternalResourceType.SERVER.name(), desc.getType());
        
        String	uri=op.get(OperationFields.URI, String.class);
        assertEquals("Mismatched name", MD5NameGenerator.getName(uri), desc.getName());
        assertEquals("Mismatched label", op.getLabel() + " " + uri, desc.getLabel());
        return desc;
	}

	private Operation assertConnectionOperation (ConnectionFactory factory) throws Exception {
		TcpConnection	conn=factory.getConnection();
		Operation		op=assertConnectionOperation(conn);
		assertEquals("Mismatched label", conn.getClass().getSimpleName() + "#getConnection", op.getLabel());
		return op;
	}

	private Operation assertConnectionOperation (TcpConnection conn) {
		Operation	op=getLastEntered();
		assertNotNull("No operation collected", op);
		assertEquals("Mismatched operation type", TcpConnectionExternalResourceAnalyzer.TYPE, op.getType());
		assertEquals("Mismatched host", conn.getHostAddress(), op.get(TcpConnectionOperationCollector.HOST_ADDRESS_ATTR, String.class));
		
		Number	port=op.get(TcpConnectionOperationCollector.PORT_ATTR, Number.class);
		assertNotNull("No port value", port);
		assertEquals("Mismatched port value", conn.getPort(), port.intValue());
		assertEquals("Mismatched server state", Boolean.valueOf(conn.isServer()), op.get(TcpConnectionOperationCollector.SERVER_ATTR, Boolean.class));
		
        String	uri=op.get(OperationFields.URI, String.class);
        assertEquals("Mismatched URI", "tcp://" + conn.getHostAddress() + ":" + conn.getPort(), uri);

		return op;
	}

	@Override
	public TcpConnectionOperationCollectionAspect getAspect() {
		return TcpConnectionOperationCollectionAspect.aspectOf();
	}
}
