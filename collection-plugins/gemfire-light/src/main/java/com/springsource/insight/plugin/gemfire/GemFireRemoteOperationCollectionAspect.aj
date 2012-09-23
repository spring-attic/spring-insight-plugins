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

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aspectj.lang.JoinPoint;

import com.gemstone.gemfire.cache.client.internal.AbstractOp;
import com.gemstone.gemfire.cache.client.internal.Connection;
import com.gemstone.gemfire.distributed.internal.ServerLocation;
import com.gemstone.gemfire.internal.cache.tier.MessageType;
import com.gemstone.gemfire.internal.cache.tier.sockets.Message;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.ExtraReflectionUtils;
import com.springsource.insight.util.ReflectionUtils;
import com.springsource.insight.util.StringUtil;


public aspect GemFireRemoteOperationCollectionAspect extends AbstractGemFireCollectionAspect {
    private static final Field MESSAGE_FIELD = findMessageField();
    
    static final String LABEL = "Gemfire Remote Opertaion";
    
    public pointcut collectionPoint () : GemFireLightPointcuts.remoteCollectionPoint() 
                && cflowbelow(GemFireLightPointcuts.queryCollectionPoint())
                && !cflowbelow(GemFireLightPointcuts.remoteCollectionPoint());

    public GemFireRemoteOperationCollectionAspect() {
		super(GemFireDefenitions.TYPE_REMOTE);
	}

	@Override
    protected Operation createOperation(final JoinPoint jp) {
    	Operation op = createBasicOperation(jp);
    	
    	AbstractOp abstractOp = (AbstractOp) jp.getThis();
    	Object[] args = jp.getArgs();
    	
    	populateConnectionData(ArrayUtil.length(args) > 0 ? args[0] : null, op);
    	populateMessageType(abstractOp, op);
    	
        return op;
    }
	
	private static void populateConnectionData(final Object arg, final Operation op) {
	    if (op == null || arg == null) {
	        return;
	    }
	    
	    if (arg instanceof Connection) {
	        Connection con = (Connection) arg;
	        ServerLocation location = con.getServer();
	        
	        if (location != null) {
	            String hostname = location.getHostName();
	            int port = location.getPort();
	            
	            populateConnectionData(hostname, port, op);
	            
	            return;
	        }
	        
	        Socket socket = con.getSocket();
	        
	        if (socket != null) {
	            SocketAddress address = socket.getRemoteSocketAddress();
	            
	            if (address instanceof InetSocketAddress) {
	                InetSocketAddress inetaddr = (InetSocketAddress) address;
	                
	                String hostname = inetaddr.getHostName();
	                int port = inetaddr.getPort();
	                
	                populateConnectionData(hostname, port, op);
	                
	                return;
	            }
	        } 
	        
	        populateConnectionData(GemFireDefenitions.FIELD_UNKNOWN, -1, op);
	    }
	}
	
	private static void populateConnectionData(final String hostname, final int port, final Operation op) {
	    op.put(GemFireDefenitions.FIELD_HOST, hostname);
	    op.put(GemFireDefenitions.FIELD_PORT, port);
	}
	
	private static void populateMessageType(final AbstractOp abstractOp, final Operation op) {
	    if (op == null || abstractOp == null) {
	        return;
	    }
	    
	    Message message = (Message) (MESSAGE_FIELD != null ? 
	                        ReflectionUtils.getField(MESSAGE_FIELD, abstractOp)
	                        :
	                        null);
	             
	    
	    if (message == null) {
	        return;
	    }
	    
	    int messageType = message.getMessageType();
	    String messageLabel = MessageType.getString(messageType);
	    
	    if (!StringUtil.isEmpty(messageLabel)) {
	        op.put(GemFireDefenitions.FIELD_MESSAGE_TYPE, messageLabel);
	    }
	}
    
	@Override
    protected String createLabel(org.aspectj.lang.Signature sig) {
        return LABEL;
    }
    
	@Override
    protected boolean addArgs() {
        return false;
    }
    
    private static Field findMessageField() {
        try {
            Field field = ExtraReflectionUtils.getAccessibleField(AbstractOp.class, "msg");
            
            if (field == null) {
                logger().warning("Unable to find AbstractOp#msg field");
            }
            
            return field;
        } catch (Exception e) {
            logger().log(Level.SEVERE, "Failed to find AbstractOp#msg field", e);
            return null;
        }
    }
    
    private static Logger logger() {
        return Logger.getLogger(GemFireRemoteOperationCollectionAspect.class.getName());
    }
    
}
