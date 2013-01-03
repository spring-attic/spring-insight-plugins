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

import java.util.Collection;
import java.util.Enumeration;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.trace.ObscuredValueMarker;

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
        throw new UnsupportedOperationException("No instance");
    }

    /**
     * Adds destination type and name to a given {@link OperationMap} only if {@code dest}
     * is not {@code null}
     * 
     * @param dest jms destination
     * @param map The {@link OperationMap} to update
     * @param marker The {@link ObscuredValueMarker} to use if a property is marked as sensitive
     * @param nameSet The {@link Collection} of properties names marked as sensitive
     * @param prefix destination type and name prefix
     * @return Same as input map
     * @throws JMSException if any occurs by accessing {@code dest} attributes
     */
    static OperationMap addDestinationDetailsToMapIfNeeded(Destination dest, OperationMap map, ObscuredValueMarker marker, Collection<String> nameSet, String prefix)
            throws JMSException {
        if (dest == null) {
            return map;
        }

        DestinationType destinationType = getDestinationType(dest);
        String destinationName = getDestinationName(dest, destinationType);
        updateAny(map, prefix+TYPE, destinationType.name(), marker, nameSet);
        updateAny(map, prefix+NAME, destinationName, marker, nameSet);
        return map;
    }

    /**
     * Creates an operation map named {@link #MESSAGE_PROPERTIES} and populates the map with {@code message} properties
     * 
     * @param op insight {@link Operation}
     * @param message jms {@link Message}
     * @param marker The {@link ObscuredValueMarker} to use if a property is marked as sensitive
     * @param nameSet The {@link Collection} of properties names marked as sensitive
     * @return Generated attributes {@link OperationMap}
     * @throws JMSException if any occurs by accessing {@code message} properties
     */
    static OperationMap extractMessageProperties(Operation op, Message message, ObscuredValueMarker marker, Collection<String> nameSet)
            throws JMSException {
        OperationMap attributesMap = op.createMap(MESSAGE_PROPERTIES);

        Enumeration<?> propertyNames = message.getPropertyNames();
        if (propertyNames != null) {
            for (Enumeration<?> propertyNameEnum = propertyNames; propertyNameEnum.hasMoreElements();) {
                String propertyName = (String) propertyNameEnum.nextElement();
                Object propertyValue = message.getObjectProperty(propertyName);
                updateAny(attributesMap, propertyName, propertyValue, marker, nameSet);
            }
        }

        return attributesMap;
    }

    /**
     * Creates an operation map name {@link #MESSAGE_HEADERS} and populates the map
     * with {@code message} headers values
     * 
     * @param op insight {@link Operation}
     * @param message jms {@link Message}
     * @param marker The {@link ObscuredValueMarker} to use if a property is marked as sensitive
     * @param nameSet The {@link Collection} of properties names marked as sensitive
     * @return The {@link OperationMap} containing the relevant extracted headers
     * @throws JMSException if any occurs by accessing {@code message} properties
     */
    static OperationMap extractMessageHeaders(Operation op, Message message, ObscuredValueMarker marker, Collection<String> nameSet)
            throws JMSException {
        OperationMap headersMap = op.createMap(MESSAGE_HEADERS);

        addDestinationDetailsToMapIfNeeded(message.getJMSDestination(), headersMap, marker, nameSet, MESSAGE_DESTINATION);
        addDestinationDetailsToMapIfNeeded(message.getJMSReplyTo(), headersMap, marker, nameSet, REPLY_TO);
        updateAny(headersMap, CORRELATION_ID, message.getJMSCorrelationID(), marker, nameSet);
        updateAny(headersMap, DELIVERY_MODE, getDeliveryMode(message.getJMSDeliveryMode()).getLabel(), marker, nameSet);
        updateAny(headersMap, EXPIRATION, Long.valueOf(message.getJMSExpiration()), marker, nameSet);
        updateAny(headersMap, MESSAGE_ID, message.getJMSMessageID(), marker, nameSet);
        updateAny(headersMap, PRIORITY, Integer.valueOf(message.getJMSPriority()), marker, nameSet);
        updateAny(headersMap, REDELIVERED, Boolean.valueOf(message.getJMSRedelivered()), marker, nameSet);

        long timestamp = message.getJMSTimestamp();
        if (timestamp > 0L) {
            updateAny(headersMap, TIMESTAMP, Long.valueOf(timestamp), marker, nameSet);
        }

        updateAny(headersMap, JMS_TYPE, message.getJMSType(), marker, nameSet);
        return headersMap;
    }

    private static OperationMap updateAny (OperationMap map, String name, Object value, ObscuredValueMarker marker, Collection<String> nameSet) {
        map.putAny(name, value);
        updateSensitiveValues(name, value, marker, nameSet);
        return map;
    }

    private static boolean updateSensitiveValues (String name, Object value, ObscuredValueMarker marker, Collection<String> nameSet) {
        if (nameSet.contains(name) && (value != null)) {
            marker.markObscured(value);
            return true;
        }

        return false;
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
            case TemporaryQueue:
                name = ((Queue) dest).getQueueName();
                break;
            case Topic:
            case TemporaryTopic:
                name = ((Topic) dest).getTopicName();
                break;
            default: // do nothing
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

        if (dest instanceof TemporaryQueue) {
            type = DestinationType.TemporaryQueue;
        } else if (dest instanceof Queue) {
            type = DestinationType.Queue;
        } else if (dest instanceof TemporaryTopic) {
            type = DestinationType.TemporaryTopic;
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
