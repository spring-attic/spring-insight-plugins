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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.TextMessage;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

public class JMSProducerCollectionAspectTest extends OperationCollectionAspectTestSupport {

    @Test
    public void testProducer() throws JMSException {
        Queue queue = mock(Queue.class);
        when(queue.getQueueName()).thenReturn("test.queue");
        
        Message _mockMessage = mock(TextMessage.class);
        
        MockProducer producer = new MockProducer(queue);
        producer.send(_mockMessage);
        
        Operation op = getLastEntered();
        
        assertEquals(JMSPluginOperationType.SEND.getOperationType(), op.getType());
        assertEquals(JMSPluginOperationType.SEND.getLabel(), op.getLabel());
        
        JMSPluginUtilsTest.assertHeaders(_mockMessage, op);
        JMSPluginUtilsTest.assertAttributes(producer.msgAttributesMap, op);
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return JMSProducerCollectionAspect.aspectOf();
    }
    
    private static class MockProducer implements MessageProducer {
        Map<String, Object> msgAttributesMap;
        final Queue queue;
        
        public MockProducer(Queue q) throws JMSException {
            if ((this.queue=q) == null) {
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
            
            msgAttributesMap = JMSPluginUtilsTest.mockAttributes(msg);
            JMSPluginUtilsTest.mockHeaders(msg);
        }

        public void send(Destination arg0, Message msg) throws JMSException {
            when(msg.getJMSDestination()).thenReturn(queue);
            
            msgAttributesMap = JMSPluginUtilsTest.mockAttributes(msg);
            JMSPluginUtilsTest.mockHeaders(msg);
        }

        public void send(Message msg, int arg1, int arg2, long arg3) throws JMSException {
            when(msg.getJMSDestination()).thenReturn(queue);
            
            msgAttributesMap = JMSPluginUtilsTest.mockAttributes(msg);
            JMSPluginUtilsTest.mockHeaders(msg);
        }

        public void send(Destination arg0, Message msg, int arg2, int arg3, long arg4) throws JMSException {
            when(msg.getJMSDestination()).thenReturn(queue);
            
            msgAttributesMap = JMSPluginUtilsTest.mockAttributes(msg);
            JMSPluginUtilsTest.mockHeaders(msg);
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
