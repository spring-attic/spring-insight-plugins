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

import java.util.Date;
import java.util.Enumeration;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.Topic;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;

/**
 * Utility class for all JMS plugin operations
 */
final class JMSPluginUtils {
    
    static final String CORRELATION_ID = "correlationId";
    static final String DELIVERY_MODE = "deliveryMode";
    static final String EXPIRATION = "expiration";
    static final String JMS_TYPE = "jmsType";
    static final String MESSAGE_PROPERTIES = "messageProperties";
    static final String MESSAGE_CONTENT = "messageContent";
    static final String MESSAGE_CONTENT_MAP = "messageContentMap";
    static final String MESSAGE_DESTINATION = "destination";
    static final String MESSAGE_HEADERS = "messageHeaders";
    static final String MESSAGE_ID = "messageId";
    static final String MESSAGE_TYPE = "messageType";
    static final String NAME = "Name";
    static final String PRIORITY = "priority";
    static final String REDELIVERED = "redelivered";
    static final String REPLY_TO = "replyTo";
    static final String TIMESTAMP = "timestamp";
    static final String TYPE = "Type";
    static final String UNKNOWN = "UNKNOWN";
    
    private JMSPluginUtils() {
        // no instance
    }
    
    /**
     * Adds destination type and name to a given {@link OperationMap} only if {@code dest} 
     * is not {@code null}
     * 
     * @param dest jms destination
     * @param map operation map
     * @param prefix destination type and name prefix
     * 
     * @throws JMSException if any occurs by accessing {@code dest} attributes
     */
    static void addDestinationDetailsToMapIfNeeded(Destination dest, OperationMap map, String prefix) throws JMSException {
        if (dest != null) {
            DestinationType destinationType = getDestinationType(dest);
            String destinationName = getDestinationName(dest, destinationType);
            
            map.put(prefix+TYPE, destinationType.name());
            map.put(prefix+NAME, destinationName);
        }
    }
    
    /**
     * Creates an operation map ({@link JMSPluginUtils#MESSAGE_PROPERTIES}) and populates the map with {@code message} properties
     * 
     * @param op insight operation
     * @param message jms message
     * 
     * @throws JMSException if any occurs by accessing {@code message} properties
     */
    @SuppressWarnings("unchecked")
    static void extractMessageProperties(Operation op, Message message) throws JMSException {
        OperationMap attributesMap = op.createMap(MESSAGE_PROPERTIES);
        
        Enumeration<String> propertyNames = message.getPropertyNames();
        if (propertyNames != null) {
            for (Enumeration<String> propertyNameEnum = propertyNames; propertyNameEnum.hasMoreElements();) {
                String propertyName = propertyNameEnum.nextElement();
                Object propertyValue = message.getObjectProperty(propertyName);
                attributesMap.putAny(propertyName, propertyValue);
            }
        }
    }
    
    /**
     * Creates an operation map ({@link JMSPluginUtils#MESSAGE_HEADERS}) and populates the map 
     * with {@code message} headers values
     * 
     * @param op insight operation
     * @param message jms message
     * 
     * @throws JMSException if any occurs by accessing {@code message} properties
     */
    static void extractMessageHeaders(Operation op, Message message) throws JMSException {
        OperationMap headersMap = op.createMap(MESSAGE_HEADERS);
        
        addDestinationDetailsToMapIfNeeded(message.getJMSDestination(), headersMap, MESSAGE_DESTINATION);
        addDestinationDetailsToMapIfNeeded(message.getJMSReplyTo(), headersMap, REPLY_TO);
        headersMap.put(CORRELATION_ID, message.getJMSCorrelationID());
        headersMap.put(DELIVERY_MODE, getDeliveryMode(message.getJMSDeliveryMode()).getLabel());
        headersMap.put(EXPIRATION, message.getJMSExpiration());
        headersMap.put(MESSAGE_ID, message.getJMSMessageID());
        headersMap.put(PRIORITY, message.getJMSPriority());
        headersMap.put(REDELIVERED, message.getJMSRedelivered());
        long timestamp = message.getJMSTimestamp();
        headersMap.putAnyNonEmpty(TIMESTAMP, timestamp > 0 ? new Date(timestamp) : null);
        headersMap.put(JMS_TYPE, message.getJMSType());
    }
    
    /**
     * Adds the message type ({@link MessageType}) to the {@code message}.<br> 
     * It also adds the {@code message} content ({@link TextMessage#getText()}) if the {@code message} is a {@link TextMessage}, <br>
     * and if the {@code message} is a {@link MapMessage} then the {@code message} content map is added.
     *  
     * @param op insight operation
     * @param message jms message
     * @throws JMSException if any occurs by accessing {@code message} properties
     */
    static void extractMessageTypeAttributes(Operation op, Message message) throws JMSException {
        MessageType messageType = MessageType.getType(message);
        messageType.handleMessage(message, op);
    }
    
    /**
     * @param deliveryMode message delivery mode ({@link DeliveryMode})
     * 
     * @return an enum representation for the {@code message} delivery mode ({@link Message#getJMSDeliveryMode()})
     */
    static DeliveryModeType getDeliveryMode(int deliveryMode) {
        DeliveryModeType mode;
        
        switch(deliveryMode) {
            case DeliveryMode.NON_PERSISTENT:
                mode = DeliveryModeType.NON_PERSISTENT;
                break;
            case DeliveryMode.PERSISTENT:
                mode = DeliveryModeType.PERSISTENT;
                break;
            default:
                mode = DeliveryModeType.UNKNOWN;
                break;
        }
        
        return mode;
    }
    
    /**
     * @param dest jms destination
     * @param type destination type
     * 
     * @return destination name ({@link Queue#getQueueName()}, {@link Topic#getTopicName()}) 
     * 
     * @throws JMSException if any occurs by accessing {@code dest} properties
     */
    static String getDestinationName(Destination dest, DestinationType type) throws JMSException {
        String name = UNKNOWN;
        
        switch(type) {
            case Queue:
                name = ((Queue) dest).getQueueName();
                break;
            case Topic:
                name = ((Topic) dest).getTopicName();
                break;
            default : // do nothing
        }
        
        return name;
    }
    
    /**
     * @param dest jms destination
     * 
     * @return {@code dest} type (queue or topic)
     */
    static DestinationType getDestinationType(Destination dest) {
        DestinationType type = DestinationType.Unknown;
        
        if (dest instanceof Queue) {
            type = DestinationType.Queue;
        } else if (dest instanceof Topic) {
            type = DestinationType.Topic;
        }
        
        return type;
    }
    
    /**
     * Extracts a {@link Message} from a {@code args}
     * 
     * @param args
     * @return a {@link Message} or {@code null} if none exists
     */
    static Message getMessage(Object[] args) {
        Message m = null;
        for(Object obj : args) {
            if (obj instanceof Message) {
                m = (Message) obj;
                break;
            }
        }
        return m;
    }
}
