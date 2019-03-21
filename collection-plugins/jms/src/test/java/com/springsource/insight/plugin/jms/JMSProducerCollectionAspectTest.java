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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

import org.junit.Test;

import com.springsource.insight.intercept.operation.Operation;

public class JMSProducerCollectionAspectTest extends AbstractJMSCollectionAspectTestSupport {
    public JMSProducerCollectionAspectTest() {
        super();
    }

    @Test
    public void testUnobscuredProducerAttributes() throws JMSException {
        runProducerTest(false);
    }

    @Test
    public void testUnobscuredProducerAttributesWithTemporaryQueue() throws JMSException {
        runProducerTestWithTemporaryQueue(false);
    }

    @Test
    public void testObscuredProducerAtttributes() throws JMSException {
        runProducerTest(true);
    }

    @Test
    public void testObscuredProducerAtttributesWithTemporaryQueue() throws JMSException {
        runProducerTestWithTemporaryQueue(true);
    }

    @Override
    public JMSProducerCollectionAspect getAspect() {
        return JMSProducerCollectionAspect.aspectOf();
    }

    private void runProducerTest(boolean obscureAttrs) throws JMSException {
        Queue queue = mock(Queue.class);
        DestinationType type = DestinationType.Queue;

        assertOperation(obscureAttrs, queue, type);
    }

    private void runProducerTestWithTemporaryQueue(boolean obscureAttrs) throws JMSException {
        Queue queue = mock(TemporaryQueue.class);
        DestinationType type = DestinationType.TemporaryQueue;

        assertOperation(obscureAttrs, queue, type);
    }

    private void assertOperation(boolean obscureAttrs, Queue queue, DestinationType type) throws JMSException {
        when(queue.getQueueName()).thenReturn("test.queue");

        Message _mockMessage = mock(TextMessage.class);
        Map<String, Object> msgAttributesMap = JMSPluginUtilsTest.mockAttributes(_mockMessage);
        JMSPluginUtilsTest.mockHeaders(_mockMessage);

        MockProducer producer = new MockProducer(queue);
        if (obscureAttrs) {
            AbstractJMSCollectionAspect.OBFUSCATED_PROPERTIES.addAll(msgAttributesMap.keySet());
        }
        producer.send(_mockMessage);

        Operation op = getLastEntered();
        assertNotNull("No operation extracted", op);
        assertEquals("Mismatched operation type", JMSPluginOperationType.SEND.getOperationType(), op.getType());
        assertEquals("Mismatched label", JMSPluginOperationType.SEND.getLabel(), op.getLabel());
        assertEquals("Mismatched destination type", type.getLabel(), op.get("destinationType", String.class));

        JMSPluginUtilsTest.assertHeaders(_mockMessage, op);
        JMSPluginUtilsTest.assertAttributes(msgAttributesMap, op);

        assertObfuscatedValuesState(msgAttributesMap, obscureAttrs);
    }

    private static class MockProducer implements MessageProducer {
        final Queue queue;

        public MockProducer(Queue q) throws JMSException {
            if ((this.queue = q) == null) {
                throw new JMSException("No queue");
            }
        }

        public void close() throws JMSException {
            // do nothing
        }

        public int getDeliveryMode() throws JMSException {
            return 0;
        }

        public Destination getDestination() throws JMSException {
            return this.queue;
        }

        public boolean getDisableMessageID() throws JMSException {
            return false;
        }

        public boolean getDisableMessageTimestamp() throws JMSException {
            return false;
        }

        public int getPriority() throws JMSException {
            return 0;
        }

        public long getTimeToLive() throws JMSException {
            return 0;
        }

        public void send(Message msg) throws JMSException {
            when(msg.getJMSDestination()).thenReturn(queue);
        }

        public void send(Destination arg0, Message msg) throws JMSException {
            when(msg.getJMSDestination()).thenReturn(queue);
        }

        public void send(Message msg, int arg1, int arg2, long arg3) throws JMSException {
            when(msg.getJMSDestination()).thenReturn(queue);
        }

        public void send(Destination arg0, Message msg, int arg2, int arg3, long arg4) throws JMSException {
            when(msg.getJMSDestination()).thenReturn(queue);
        }

        public void setDeliveryMode(int arg0) throws JMSException {
            // do nothing
        }

        public void setDisableMessageID(boolean arg0) throws JMSException {
            // do nothing
        }

        public void setDisableMessageTimestamp(boolean arg0) throws JMSException {
            // do nothing
        }

        public void setPriority(int arg0) throws JMSException {
            // do nothing
        }

        public void setTimeToLive(long arg0) throws JMSException {
            // do nothing
        }
    }

}
