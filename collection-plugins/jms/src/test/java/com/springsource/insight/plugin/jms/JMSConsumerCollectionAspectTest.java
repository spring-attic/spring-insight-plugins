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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.TextMessage;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

public class JMSConsumerCollectionAspectTest extends OperationCollectionAspectTestSupport {
	public JMSConsumerCollectionAspectTest () {
		super();
	}

    @Test
    public void testConsumer() throws JMSException {
        Queue queue = mock(Queue.class);
        when(queue.getQueueName()).thenReturn("test.queue");
        
        MockConsumer consumer = new MockConsumer(queue);
        Message message = consumer.receive();
        
        Operation op = getLastEntered();
        
        assertEquals(JMSPluginOperationType.RECEIVE.getOperationType(), op.getType());
        assertEquals(JMSPluginOperationType.RECEIVE.getLabel(), op.getLabel());
        
        JMSPluginUtilsTest.assertHeaders(message, op);
        JMSPluginUtilsTest.assertAttributes(consumer.msgAttributesMap, op);
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return JMSConsumerCollectionAspect.aspectOf();
    }
    
    private static class MockConsumer implements MessageConsumer {
        MessageConsumer _mock;
        TextMessage _mockMessage;
        Map<String, Object> msgAttributesMap;
        
        public MockConsumer(Queue queue) throws JMSException {
            _mockMessage = mock(TextMessage.class);
            when(_mockMessage.getJMSDestination()).thenReturn(queue);
            
            msgAttributesMap = JMSPluginUtilsTest.mockAttributes(_mockMessage);
            JMSPluginUtilsTest.mockHeaders(_mockMessage);
            
            _mock = mock(MessageConsumer.class);
            when(_mock.receive()).thenReturn(_mockMessage);
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
            return _mock.receive();
        }

        public Message receive(long arg0) throws JMSException {
            return _mock.receive();
        }

        public Message receiveNoWait() throws JMSException {
            return _mock.receive();
        }

        public void setMessageListener(MessageListener arg0) throws JMSException {
            // do nothing
        }
    }

}
