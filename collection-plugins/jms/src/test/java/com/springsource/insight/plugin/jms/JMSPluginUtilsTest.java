/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.plugin.jms;

import static com.springsource.insight.plugin.jms.JMSPluginUtils.CORRELATION_ID;
import static com.springsource.insight.plugin.jms.JMSPluginUtils.DELIVERY_MODE;
import static com.springsource.insight.plugin.jms.JMSPluginUtils.EXPIRATION;
import static com.springsource.insight.plugin.jms.JMSPluginUtils.MESSAGE_CONTENT;
import static com.springsource.insight.plugin.jms.JMSPluginUtils.MESSAGE_CONTENT_MAP;
import static com.springsource.insight.plugin.jms.JMSPluginUtils.MESSAGE_HEADERS;
import static com.springsource.insight.plugin.jms.JMSPluginUtils.MESSAGE_ID;
import static com.springsource.insight.plugin.jms.JMSPluginUtils.MESSAGE_PROPERTIES;
import static com.springsource.insight.plugin.jms.JMSPluginUtils.MESSAGE_TYPE;
import static com.springsource.insight.plugin.jms.JMSPluginUtils.NAME;
import static com.springsource.insight.plugin.jms.JMSPluginUtils.PRIORITY;
import static com.springsource.insight.plugin.jms.JMSPluginUtils.REDELIVERED;
import static com.springsource.insight.plugin.jms.JMSPluginUtils.TYPE;
import static com.springsource.insight.plugin.jms.JMSPluginUtils.addDestinationDetailsToMapIfNeeded;
import static com.springsource.insight.plugin.jms.JMSPluginUtils.extractMessageHeaders;
import static com.springsource.insight.plugin.jms.JMSPluginUtils.extractMessageProperties;
import static com.springsource.insight.plugin.jms.JMSPluginUtils.extractMessageTypeAttributes;
import static com.springsource.insight.plugin.jms.JMSPluginUtils.getDeliveryMode;
import static com.springsource.insight.plugin.jms.JMSPluginUtils.getMessage;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;

import com.springsource.insight.collection.test.AbstractCollectionTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.util.MathUtil;

public class JMSPluginUtilsTest extends AbstractCollectionTestSupport {
    public JMSPluginUtilsTest() {
        super();
    }

    @Test
    public void testAddDestinationDetailsToMapIfNeeded() throws JMSException {
        Queue queue = mock(Queue.class);
        when(queue.getQueueName()).thenReturn("mock-queue");

        Operation op = new Operation();
        OperationMap map = op.createMap("test-map");

        addDestinationDetailsToMapIfNeeded(queue, map, null, Collections.<String>emptyList(), "test");

        String type = map.get("test" + TYPE, String.class);
        String name = map.get("test" + NAME, String.class);

        assertNotNull(type);
        assertNotNull(name);

        assertEquals(type, DestinationType.Queue.name());
        assertEquals(name, "mock-queue");

        queue = null;
        op = new Operation();
        map = op.createMap("test-map");

        addDestinationDetailsToMapIfNeeded(queue, map, null, Collections.<String>emptyList(), "test");
        type = map.get("test" + TYPE, String.class);
        name = map.get("test" + NAME, String.class);

        assertNull(type);
        assertNull(name);
    }

    @Test
    public void testExtractMessageAttributes() throws JMSException {
        Message message = mock(Message.class);

        final Map<String, Object> mockMap = mockAttributes(message);

        Operation op = new Operation();
        extractMessageProperties(op, message, null, Collections.<String>emptyList());
        assertAttributes(mockMap, op);
    }

    static OperationMap assertAttributes(final Map<String, ?> mockMap, Operation op) {
        OperationMap opMap = op.get(MESSAGE_PROPERTIES, OperationMap.class);
        assertNotNull("Missing " + MESSAGE_PROPERTIES + " map", opMap);

        for (String key : mockMap.keySet()) {
            Object mockValue = mockMap.get(key);
            if (mockValue instanceof Number) {
                Number opValue = opMap.get(key, Number.class);
                assertNotNull("Missing number value for key=" + key, opValue);
                if (MathUtil.isIntegralNumber(opValue)) {
                    assertEquals("Mismatched integral value for key=" + key, ((Number) mockValue).longValue(), opValue.longValue());
                } else {
                    assertEquals("Mismatched integral value for key=" + key, ((Number) mockValue).doubleValue(), opValue.doubleValue(), 0.0001d);
                }
            } else {
                assertEquals("Mismatched value for key=" + key, mockValue, opMap.get(key));
            }
        }

        return opMap;
    }

    static Map<String, Object> mockAttributes(Message message) throws JMSException {
        final Map<String, Object> mockMap = new HashMap<String, Object>();
        mockMap.put("test-string", "test-value");
        mockMap.put("test-int", Integer.valueOf(1));
        mockMap.put("host", "jms-host");
        mockMap.put("port", Integer.valueOf(8070));

        when(message.getPropertyNames()).thenReturn(new Enumeration<String>() {
            Iterator<String> iter = mockMap.keySet().iterator();

            public boolean hasMoreElements() {
                return iter.hasNext();
            }

            public String nextElement() {
                return iter.next();
            }
        });

        when(message.getObjectProperty(argThat(new BaseMatcher<String>() {
            public boolean matches(Object val) {
                return "test-string".equals(val);
            }

            public void describeTo(Description desc) {
                // do nothing
            }
        }))).thenReturn(mockMap.get("test-string"));

        when(message.getObjectProperty(argThat(new BaseMatcher<String>() {
            public boolean matches(Object val) {
                return "test-int".equals(val);
            }

            public void describeTo(Description desc) {
                // do nothing
            }
        }))).thenReturn(mockMap.get("test-int"));

        when(message.getObjectProperty(argThat(new BaseMatcher<String>() {
            public boolean matches(Object val) {
                return "port".equals(val);
            }

            public void describeTo(Description desc) {
                // do nothing
            }
        }))).thenReturn(mockMap.get("port"));

        when(message.getObjectProperty(argThat(new BaseMatcher<String>() {
            public boolean matches(Object val) {
                return "host".equals(val);
            }

            public void describeTo(Description desc) {
                // do nothing
            }
        }))).thenReturn(mockMap.get("host"));
        return mockMap;
    }

    @Test
    public void testExtractMessageHeaders() throws JMSException {
        Message message = mock(Message.class);

        mockHeaders(message);

        Operation op = new Operation();

        extractMessageHeaders(op, message, null, Collections.<String>emptyList());
        assertHeaders(message, op);
    }

    static OperationMap assertHeaders(Message message, Operation op) throws JMSException {
        OperationMap opMap = op.get(MESSAGE_HEADERS, OperationMap.class);
        assertNotNull("Missing " + MESSAGE_HEADERS + " map", opMap);

        assertEquals(CORRELATION_ID, message.getJMSCorrelationID(), opMap.get(CORRELATION_ID, String.class));
        assertEquals(DELIVERY_MODE, getDeliveryMode(message.getJMSDeliveryMode()).getLabel(), opMap.get(DELIVERY_MODE, String.class));
        assertEquals(EXPIRATION, message.getJMSExpiration(), opMap.getLong(EXPIRATION, (-1L)));
        assertEquals(MESSAGE_ID, message.getJMSMessageID(), opMap.get(MESSAGE_ID, String.class));
        assertEquals(PRIORITY, message.getJMSPriority(), opMap.getInt(PRIORITY, (-1)));
        assertEquals(REDELIVERED, Boolean.valueOf(message.getJMSRedelivered()), opMap.get(REDELIVERED, Boolean.class));

        return opMap;
    }

    @SuppressWarnings("boxing")
    static void mockHeaders(Message message) throws JMSException {
        when(message.getJMSCorrelationID()).thenReturn("1");
        when(message.getJMSDeliveryMode()).thenReturn(DeliveryMode.PERSISTENT);
        when(message.getJMSExpiration()).thenReturn(0L);
        when(message.getJMSMessageID()).thenReturn("2");
        when(message.getJMSPriority()).thenReturn(3);
        when(message.getJMSRedelivered()).thenReturn(true);
    }

    @Test
    public void testExtractMapMessageTypeAttributes() throws JMSException {
        MapMessage mapMessage = mock(MapMessage.class);

        final Map<String, String> mockMap = new HashMap<String, String>();
        mockMap.put("test-key", "test-value");

        when(mapMessage.getMapNames()).thenReturn(new Enumeration<String>() {
            Iterator<String> iter = mockMap.keySet().iterator();

            public boolean hasMoreElements() {
                return iter.hasNext();
            }

            public String nextElement() {
                return iter.next();
            }
        });

        when(mapMessage.getObject(argThat(new BaseMatcher<String>() {
            public boolean matches(Object val) {
                return "test-key".equals(val);
            }

            public void describeTo(Description desc) {
                // do nothing
            }
        }))).thenReturn(mockMap.get("test-key"));

        Operation op = new Operation();

        extractMessageTypeAttributes(op, mapMessage);

        String type = op.get(MESSAGE_TYPE, String.class);
        OperationMap contentMap = op.get(MESSAGE_CONTENT_MAP, OperationMap.class);

        assertNotNull(contentMap);
        assertEquals(mockMap.size(), contentMap.size());
        assertEquals(mockMap.get("test-key"), contentMap.get("test-key"));
        assertEquals(MessageType.MapMessage.name(), type);
        assertNull(op.get(MESSAGE_CONTENT));
    }

    @Test
    public void testExtractTextMessageTypeAttributes() throws JMSException {
        TextMessage txtMessage = mock(TextMessage.class);
        when(txtMessage.getText()).thenReturn("test-text");

        Operation op = new Operation();

        extractMessageTypeAttributes(op, txtMessage);

        String type = op.get(MESSAGE_TYPE, String.class);
        String content = op.get(MESSAGE_CONTENT, String.class);

        assertEquals(MessageType.TextMessage.name(), type);
        assertEquals("test-text", content);
        assertNull(op.get(MESSAGE_CONTENT_MAP));
    }

    @Test
    public void testExtractBytesMessageTypeAttributes() throws JMSException {
        BytesMessage bytesMessage = mock(BytesMessage.class);

        Operation op = new Operation();

        extractMessageTypeAttributes(op, bytesMessage);

        String type = op.get(MESSAGE_TYPE, String.class);

        assertEquals(MessageType.BytesMessage.name(), type);
        assertNull(op.get(MESSAGE_CONTENT));
        assertNull(op.get(MESSAGE_CONTENT_MAP));
    }

    @Test
    public void testExtractObjectMessageTypeAttributes() throws JMSException {
        ObjectMessage objectMessage = mock(ObjectMessage.class);

        Operation op = new Operation();

        extractMessageTypeAttributes(op, objectMessage);

        String type = op.get(MESSAGE_TYPE, String.class);

        assertEquals(MessageType.ObjectMessage.name(), type);
        assertNull(op.get(MESSAGE_CONTENT));
        assertNull(op.get(MESSAGE_CONTENT_MAP));
    }

    @Test
    public void testExtractStreamMessageTypeAttributes() throws JMSException {
        StreamMessage objectMessage = mock(StreamMessage.class);

        Operation op = new Operation();

        extractMessageTypeAttributes(op, objectMessage);

        String type = op.get(MESSAGE_TYPE, String.class);

        assertEquals(MessageType.StreamMessage.name(), type);
        assertNull(op.get(MESSAGE_CONTENT));
        assertNull(op.get(MESSAGE_CONTENT_MAP));
    }

    @Test
    public void testGetDeliveryMode() {
        int deliveryMode = DeliveryMode.NON_PERSISTENT;

        DeliveryModeType type = getDeliveryMode(deliveryMode);
        assertEquals(DeliveryModeType.NON_PERSISTENT, type);

        deliveryMode = DeliveryMode.PERSISTENT;

        type = getDeliveryMode(deliveryMode);
        assertEquals(DeliveryModeType.PERSISTENT, type);

        deliveryMode = -1;

        type = getDeliveryMode(deliveryMode);
        assertEquals(DeliveryModeType.UNKNOWN, type);
    }

    @Test
    public void testGetMessage() {
        Message message = mock(Message.class);

        Object[] args = new Object[]{message};

        Message fromUtil = getMessage(args);

        assertSame(message, fromUtil);
    }
}
