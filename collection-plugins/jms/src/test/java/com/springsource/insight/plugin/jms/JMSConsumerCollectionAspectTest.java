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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

import org.junit.Before;
import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.intercept.operation.Operation;

public class JMSConsumerCollectionAspectTest extends AbstractJMSCollectionAspectTestSupport {
    public JMSConsumerCollectionAspectTest () {
        super();
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
        AbstractJMSCollectionAspect.OBFUSCATED_PROPERTIES.clear();
    }

    @Test
    public void testUnobscuredProducerAttributes() throws JMSException {
        runConsumerTest(false);
    }

    @Test
    public void testUnobscuredProducerAttributesWithTemporaryQueue() throws JMSException {
        runConsumerTestWithTemporaryQueue(false);
    }

    @Test
    public void testObscuredProducerAtttributes() throws JMSException {
        runConsumerTest(true);
    }

    @Test
    public void testObscuredProducerAtttributesWithTemporaryQueue() throws JMSException {
        runConsumerTestWithTemporaryQueue(true);
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return JMSConsumerCollectionAspect.aspectOf();
    }

    private void runConsumerTest (boolean obscureAttrs) throws JMSException {
        Queue queue = mock(Queue.class);
        DestinationType type = DestinationType.Queue;

        assertOperation(obscureAttrs, queue, type);
    }

    private void runConsumerTestWithTemporaryQueue(boolean obscureAttrs) throws JMSException {
        Queue queue = mock(TemporaryQueue.class);
        DestinationType type = DestinationType.TemporaryQueue;

        assertOperation(obscureAttrs, queue, type);
    }

    private void assertOperation(boolean obscureAttrs, Queue queue, DestinationType type) throws JMSException {
        when(queue.getQueueName()).thenReturn("test.queue");

        MockConsumer consumer = new MockConsumer(queue);
        Map<String, Object> msgAttributesMap = consumer.msgAttributesMap;
        if (obscureAttrs) {
            AbstractJMSCollectionAspect.OBFUSCATED_PROPERTIES.addAll(msgAttributesMap.keySet());
        }

        Message message = consumer.receive();
        assertNotNull("No message consumed", message);

        Operation op = getLastEntered();
        assertNotNull("No operation collected", op);
        assertEquals("Mismatched operation type", JMSPluginOperationType.RECEIVE.getOperationType(), op.getType());
        assertEquals("Mismatched operation label", JMSPluginOperationType.RECEIVE.getLabel(), op.getLabel());
        assertEquals("Mismatched destination type", type.getLabel(), op.get("destinationType", String.class));

        JMSPluginUtilsTest.assertHeaders(message, op);
        JMSPluginUtilsTest.assertAttributes(consumer.msgAttributesMap, op);

        assertObfuscatedValuesState(msgAttributesMap, obscureAttrs);
    }

    private static class MockConsumer implements MessageConsumer {
        private final TextMessage _mockMessage;
        final Map<String, Object> msgAttributesMap;

        public MockConsumer(Queue queue) throws JMSException {
            _mockMessage = mock(TextMessage.class);
            when(_mockMessage.getText()).thenReturn(getClass().getSimpleName());
            when(_mockMessage.getJMSDestination()).thenReturn(queue);
            when(_mockMessage.getJMSType()).thenReturn(TextMessage.class.getSimpleName());
            when(_mockMessage.getJMSCorrelationID()).thenReturn("3777347");

            msgAttributesMap = JMSPluginUtilsTest.mockAttributes(_mockMessage);
            JMSPluginUtilsTest.mockHeaders(_mockMessage);
        }

        public void close() throws JMSException {
            // do nothing
        }

        public MessageListener getMessageListener() throws JMSException {
            return null;
        }

        public String getMessageSelector() throws JMSException {
            return null;
        }

        public Message receive() throws JMSException {
            return _mockMessage;
        }

        public Message receive(long timeout) throws JMSException {
            return _mockMessage;
        }

        public Message receiveNoWait() throws JMSException {
            return _mockMessage;
        }

        public void setMessageListener(MessageListener listener) throws JMSException {
            // do nothing
        }
    }

}
