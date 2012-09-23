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
import org.mockito.exceptions.verification.WantedButNotInvoked;

import com.gemstone.gemfire.cache.client.internal.AbstractOp;
import com.gemstone.gemfire.cache.client.internal.Connection;
import com.gemstone.gemfire.cache.client.internal.ConnectionStats;
import com.gemstone.gemfire.cache.query.FunctionDomainException;
import com.gemstone.gemfire.cache.query.NameResolutionException;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.QueryInvocationTargetException;
import com.gemstone.gemfire.cache.query.QueryStatistics;
import com.gemstone.gemfire.cache.query.TypeMismatchException;
import com.gemstone.gemfire.distributed.internal.ServerLocation;
import com.gemstone.gemfire.i18n.LogWriterI18n;
import com.gemstone.gemfire.internal.cache.tier.MessageType;
import com.gemstone.gemfire.internal.cache.tier.sockets.Message;
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.util.ExceptionUtils;

public class GemFireRemoteCollectionAspectTest extends GemFireAspectTestSupport {
    
    private static final String TEST_ADDRESS = "10.10.10.10";
    private static final int    TEST_PORT    = 6677;
    
    @Mock Connection con;
    @Mock Socket sock;
    @Mock ServerLocation location;
    @Mock LogWriterI18n logWriter;
    
    public GemFireRemoteCollectionAspectTest () {
    	super();
    	
    	MockitoAnnotations.initMocks(this);
    	
    	InetSocketAddress address = new InetSocketAddress(TEST_ADDRESS, TEST_PORT);
    	
    	Mockito.when(location.getHostName()).thenReturn(TEST_ADDRESS);
    	Mockito.when(location.getPort()).thenReturn(TEST_PORT);
    	
    	Mockito.when(sock.getRemoteSocketAddress()).thenReturn(address);
    }
    
    @Test(expected=WantedButNotInvoked.class)
    public void testWithoutQuery() throws Exception {
        MockOp op = new MockOp(logWriter, MessageType.PUT, 0);
        
        Mockito.when(con.getServer()).thenReturn(location);
        Mockito.when(con.getSocket()).thenReturn(null);
        
        assertNull("Getting socket from connection", con.getSocket());
        
        op.sendMessage(con);
        
        Operation lastOperation = getLastEntered();
        
        assertOperation(lastOperation, MessageType.PUT);
    }
    
    @Test
    public void testLocationFromServerLocation() throws Exception {
        MockQuery op = new MockQuery(logWriter, MessageType.PUT, 0, con);
        
        Mockito.when(con.getServer()).thenReturn(location);
        Mockito.when(con.getSocket()).thenReturn(null);
        
        assertNull("Getting socket from connection", con.getSocket());
        
        op.execute();
        
        Operation lastOperation = getLastEntered();
        
        assertOperation(lastOperation, MessageType.PUT);
    }
    
    @Test
    public void testLocationFromSocket() throws Exception {
        MockQuery op = new MockQuery(logWriter, MessageType.QUERY, 0, con);
        
        Mockito.when(con.getServer()).thenReturn(null);
        Mockito.when(con.getSocket()).thenReturn(sock);
        
        assertNull("Getting server location from connection", con.getServer());
        
        op.execute();
        
        Operation lastOperation = getLastEntered();
        
        assertOperation(lastOperation, MessageType.QUERY);
    }
        
	private void assertOperation(Operation lastOperation, int msgType) {
	    String hostname    = lastOperation.get(GemFireDefenitions.FIELD_HOST, String.class);
	    Integer port       = lastOperation.get(GemFireDefenitions.FIELD_PORT, Integer.class);
	    String messageType = lastOperation.get(GemFireDefenitions.FIELD_MESSAGE_TYPE, String.class);
	    String messageLbl  = lastOperation.getLabel();
	    OperationType type = lastOperation.getType();
	    
	    assertEquals("GemFire remote operation host", TEST_ADDRESS, hostname);
	    assertEquals("GemFire remote operation port", TEST_PORT, port, 0d);
	    assertEquals("GemFire remote operation messageType", MessageType.getString(msgType), messageType);
	    assertEquals("GemFire remote operation label", GemFireRemoteOperationCollectionAspect.LABEL, messageLbl);
	    assertEquals("GemFire remote operation type", GemFireDefenitions.TYPE_REMOTE.getType(), type);
    }

    @Override
	public OperationCollectionAspectSupport getAspect() {
		return GemFireRemoteOperationCollectionAspect.aspectOf();
	}
    
    private static final class MockQuery extends MockOp implements Query {

        private Connection con;

        protected MockQuery(LogWriterI18n lw, int msgType, int msgParts, Connection con) {
            super(lw, msgType, msgParts);
            this.con = con;
        }

        @Deprecated
        public void compile() throws TypeMismatchException,
                NameResolutionException {
            
        }

        public Object execute() throws FunctionDomainException,
                TypeMismatchException, NameResolutionException,
                QueryInvocationTargetException {
            
            try {
                super.sendMessage(con);
            } catch (Exception e) {
                e.printStackTrace();
                ExceptionUtils.toRuntimeException(e);
            }
            
            return "test-object";
        }

        public Object execute(Object[] params) throws FunctionDomainException,
                TypeMismatchException, NameResolutionException,
                QueryInvocationTargetException {
            return null;
        }

        public String getQueryString() {
            return "test";
        }

        public QueryStatistics getStatistics() {
            return null;
        }

        @Deprecated
        public boolean isCompiled() {
            return false;
        }

        @Override
        protected void endAttempt(ConnectionStats arg0, long arg1) {
        }

        @Override
        protected void endSendAttempt(ConnectionStats arg0, long arg1) {
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
            return 0;
        }
    }
	
	private static class MockOp extends AbstractOp {

        protected MockOp(LogWriterI18n lw, int msgType, int msgParts) {
            super(lw, msgType, msgParts);
        }
        
        @Override
        protected void sendMessage(Connection conn) throws Exception {
            System.out.println("MockOp sending message to connection: " + conn);
        }

        @Override
        protected void endAttempt(ConnectionStats arg0, long arg1) {
        }

        @Override
        protected void endSendAttempt(ConnectionStats arg0, long arg1) {
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
            return 0;
        }
	}
}
