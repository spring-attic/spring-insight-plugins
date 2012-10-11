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

package com.springsource.insight.plugin.gemfire;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.gemstone.gemfire.cache.client.internal.AbstractOp;
import com.gemstone.gemfire.cache.client.internal.Connection;
import com.gemstone.gemfire.cache.client.internal.ConnectionStats;
import com.gemstone.gemfire.distributed.internal.ServerLocation;
import com.gemstone.gemfire.i18n.LogWriterI18n;
import com.gemstone.gemfire.internal.cache.tier.MessageType;
import com.gemstone.gemfire.internal.cache.tier.sockets.Message;
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

public class GemFireRemoteCollectionAspectTest extends GemFireAspectTestSupport {
    
    private static final String TEST_ADDRESS = "10.10.10.10";
    private static final int    TEST_PORT    = 6677;
    
    @Mock private Connection con;
    @Mock private Socket sock;
    @Mock private ServerLocation location;
    @Mock private LogWriterI18n logWriter;
    
    @SuppressWarnings("boxing")
	public GemFireRemoteCollectionAspectTest () {
    	MockitoAnnotations.initMocks(this);
    	
    	InetSocketAddress address = new InetSocketAddress(TEST_ADDRESS, TEST_PORT);
    	
    	Mockito.when(location.getHostName()).thenReturn(TEST_ADDRESS);
    	Mockito.when(location.getPort()).thenReturn(TEST_PORT);
    	
    	Mockito.when(sock.getRemoteSocketAddress()).thenReturn(address);
    }
    
    @Test
    public void testLocationFromServerLocation() throws Exception {
        MockOp op = new MockOp(logWriter, MessageType.PUT, 0);
        
        Mockito.when(con.getServer()).thenReturn(location);
        Mockito.when(con.getSocket()).thenReturn(null);
        
        assertNull("Getting socket from connection", con.getSocket());
        
        op.sendMessage(con);
        
        Operation lastOperation = getLastEntered();
        
        assertOperation(lastOperation, MessageType.PUT);
    }
    
    @Test
    public void testLocationFromSocket() throws Exception {
        MockOp op = new MockOp(logWriter, MessageType.QUERY, 0);
        
        Mockito.when(con.getServer()).thenReturn(null);
        Mockito.when(con.getSocket()).thenReturn(sock);
        
        assertNull("Getting server location from connection", con.getServer());
        
        op.sendMessage(con);
        
        Operation lastOperation = getLastEntered();
        
        assertOperation(lastOperation, MessageType.QUERY);
    }
        
	private void assertOperation(Operation lastOperation, int msgType) {
	    String hostname    = lastOperation.get(GemFireDefenitions.FIELD_HOST, String.class);
	    Number port       = lastOperation.get(GemFireDefenitions.FIELD_PORT, Number.class);
	    assertNotNull("No port value", port);

	    String messageType = lastOperation.get(GemFireDefenitions.FIELD_MESSAGE_TYPE, String.class);
	    String messageLbl  = lastOperation.getLabel();
	    OperationType type = lastOperation.getType();
	    
	    assertEquals("GemFire remote operation host", TEST_ADDRESS, hostname);
	    assertEquals("GemFire remote operation port", TEST_PORT, port.intValue(), 0);
	    assertEquals("GemFire remote operation messageType", MessageType.getString(msgType), messageType);
	    assertEquals("GemFire remote operation label", GemFireRemoteOperationCollectionAspect.LABEL, messageLbl);
	    assertEquals("GemFire remote operation type", GemFireDefenitions.TYPE_REMOTE.getType(), type);
    }

    @Override
	public OperationCollectionAspectSupport getAspect() {
		return GemFireRemoteOperationCollectionAspect.aspectOf();
	}
	
	private static final class MockOp extends AbstractOp {

        protected MockOp(LogWriterI18n lw, int msgType, int msgParts) {
            super(lw, msgType, msgParts);
        }
        
        @Override
        protected void sendMessage(Connection conn) throws Exception {
            System.out.println("MockOp sending message to connection: " + conn);
        }

        @Override
        protected void endAttempt(ConnectionStats arg0, long arg1) {
        	// ignored
        }

        @Override
        protected void endSendAttempt(ConnectionStats arg0, long arg1) {
        	// ignored
        }

        @Override
        protected boolean isErrorResponse(int arg0) {
            return false;
        }

        @Override
        protected Object processResponse(Message arg0) throws Exception {
            return null;
        }

        @Override
        protected long startAttempt(ConnectionStats arg0) {
            return 0L;
        }
	}
}
