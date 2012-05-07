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

import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;

/**
 * This enum values reflects all five types of JMS messages
 * 
 * @author shachar
 *
 */
enum MessageType {
    /**
     * Bytes body message
     */
    BytesMessage {
        @Override
        public boolean isTypeOf(Message message) {
            return message instanceof BytesMessage;
        }
    },
    
    /**
     * Map body message
     */
    MapMessage {
        @Override
        @SuppressWarnings("unchecked")
        public void handleMessage(Message message, Operation op) throws JMSException {
            super.handleMessage(message, op);
            
            OperationMap contentMap = op.createMap(JMSPluginUtils.MESSAGE_CONTENT_MAP);
            
            MapMessage mapMessage = (MapMessage) message;
            
            for(Enumeration<String> entryNameEnum = mapMessage.getMapNames(); entryNameEnum.hasMoreElements();) {
                String entryName = entryNameEnum.nextElement();
                Object entryValue = mapMessage.getObject(entryName);
                
                contentMap.putAny(entryName, entryValue);
            }
        }

        @Override
        public boolean isTypeOf(Message message) {
            return message instanceof MapMessage;
        }
    },
    
    /**
     * Object body message
     */
    ObjectMessage {
        @Override
        public boolean isTypeOf(Message message) {
            return message instanceof ObjectMessage;
        }
    },
    
    /**
     * Stream body message
     */
    StreamMessage {
        @Override
        public boolean isTypeOf(Message message) {
            return message instanceof StreamMessage;
        }
    }, 
    
    /**
     * Text body message
     */
    TextMessage {
        @Override
        public void handleMessage(Message message, Operation op) throws JMSException {
            super.handleMessage(message, op);
            op.put(JMSPluginUtils.MESSAGE_CONTENT, ((TextMessage) message).getText());
        }

        @Override
        public boolean isTypeOf(Message message) {
            return message instanceof TextMessage;
        }
    }, 
    
    /**
     * in case the message type isn't one of the above 
     */
    UNKNOWN {
        @Override
        public boolean isTypeOf(Message message) {
            return false;
        }
    };
    
    /**
     * add message specific data to the operation
     * 
     * @param message jms message
     * @param op insight operation
     * @throws JMSException 
     */
    public void handleMessage(Message message, Operation op) throws JMSException {
        op.put(JMSPluginUtils.MESSAGE_TYPE, name());
    }
    
    /**
     * @param message jms message
     * @return true if this type matches to the message type 
     */
    public abstract boolean isTypeOf(Message message);
    
    /**
     * @param message jms message
     * @return the first MessageType where MessageType.isTypeOf(message) returned true
     */
    public static MessageType getType(Message message) {
        MessageType messageType = MessageType.UNKNOWN;
        
        for(MessageType type : values()) {
            if (type.isTypeOf(message)) {
                messageType = type;
                break;
            }
        }
        
        return messageType;
    }
}