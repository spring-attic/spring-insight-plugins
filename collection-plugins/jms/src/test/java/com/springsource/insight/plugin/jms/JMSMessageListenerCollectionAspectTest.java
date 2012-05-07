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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.TextMessage;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

public class JMSMessageListenerCollectionAspectTest extends OperationCollectionAspectTestSupport {

    @Test
    public void testMessageListener() throws JMSException {
        Queue queue = mock(Queue.class);
        when(queue.getQueueName()).thenReturn("test.queue");
        
        Message _mockMessage = mock(TextMessage.class);
        when(_mockMessage.getJMSDestination()).thenReturn(queue);
        
        Map<String, Object> msgAttributesMap = JMSPluginUtilsTest.mockAttributes(_mockMessage);
        JMSPluginUtilsTest.mockHeaders(_mockMessage);
        
        MockMessageListener listener = new MockMessageListener();
        listener.onMessage(_mockMessage);
        
        Operation op = getLastEntered();
        
        assertEquals(JMSPluginOperationType.LISTENER_RECEIVE.getOperationType(), op.getType());
        assertEquals(JMSPluginOperationType.LISTENER_RECEIVE.getLabel(), op.getLabel());
        
        JMSPluginUtilsTest.assertHeaders(_mockMessage, op);
        JMSPluginUtilsTest.assertAttributes(msgAttributesMap, op);
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return JMSMessageListenerCollectionAspect.aspectOf();
    }

    private static class MockMessageListener implements MessageListener {
        public void onMessage(Message msg) {
            // do nothing
        }
    }

}
