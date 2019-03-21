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
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

import org.junit.Before;
import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.intercept.operation.Operation;

public class JMSMessageListenerCollectionAspectTest extends AbstractJMSCollectionAspectTestSupport {
    public JMSMessageListenerCollectionAspectTest() {
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
        runListenerTest(false);
    }

    @Test
    public void testUnobscuredProducerAttributesWithTemporaryQueue() throws JMSException {
        runListenerTestWithTemporaryQueue(false);
    }

    @Test
    public void testObscuredProducerAtttributes() throws JMSException {
        runListenerTest(true);
    }

    @Test
    public void testObscuredProducerAtttributesWithTemporaryQueue() throws JMSException {
        runListenerTestWithTemporaryQueue(true);
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return JMSMessageListenerCollectionAspect.aspectOf();
    }

    private void runListenerTest(boolean obscureAttrs) throws JMSException {
        Queue queue = mock(Queue.class);
        DestinationType type = DestinationType.Queue;

        assertOperation(obscureAttrs, queue, type);
    }

    private void runListenerTestWithTemporaryQueue(boolean obscureAttrs) throws JMSException {
        Queue queue = mock(TemporaryQueue.class);
        DestinationType type = DestinationType.TemporaryQueue;

        assertOperation(obscureAttrs, queue, type);
    }

    private void assertOperation(boolean obscureAttrs, Queue queue, DestinationType type) throws JMSException {
        when(queue.getQueueName()).thenReturn("test.queue");

        Message _mockMessage = mock(TextMessage.class);
        when(_mockMessage.getJMSDestination()).thenReturn(queue);

        Map<String, Object> msgAttributesMap = JMSPluginUtilsTest.mockAttributes(_mockMessage);
        if (obscureAttrs) {
            AbstractJMSCollectionAspect.OBFUSCATED_PROPERTIES.addAll(msgAttributesMap.keySet());
        }
        JMSPluginUtilsTest.mockHeaders(_mockMessage);

        MockMessageListener listener = new MockMessageListener();
        listener.onMessage(_mockMessage);
        Message lastMessage = listener.getLastMessage();
        assertSame("Mismatched invoked listener messages", _mockMessage, lastMessage);

        Operation op = getLastEntered();
        assertNotNull("No operation collected", op);
        assertEquals("Mismatched operation type", JMSPluginOperationType.LISTENER_RECEIVE.getOperationType(), op.getType());
        assertEquals("Mismatched operation label", JMSPluginOperationType.LISTENER_RECEIVE.getLabel(), op.getLabel());
        assertEquals("Mismatched destination type", type.getLabel(), op.get("destinationType", String.class));

        JMSPluginUtilsTest.assertHeaders(_mockMessage, op);
        JMSPluginUtilsTest.assertAttributes(msgAttributesMap, op);

        assertObfuscatedValuesState(msgAttributesMap, obscureAttrs);
    }

    private static class MockMessageListener implements MessageListener {
        private Message lastMessage;

        public MockMessageListener() {
            super();
        }

        Message getLastMessage() {
            return lastMessage;
        }

        public void onMessage(Message msg) {
            lastMessage = msg;
        }
    }

}
