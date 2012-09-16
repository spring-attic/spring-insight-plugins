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
import com.springsource.insight.intercept.operation.Operation;

public class JMSMessageListenerCollectionAspectTest extends AbstractJMSCollectionAspectTestSupport {
	public JMSMessageListenerCollectionAspectTest () {
		super();
	}

    @Test
    public void testUnobscuredMessageListenerValues() throws JMSException {
    	runListenerTest(false);
    }

    @Test
    public void testObscuredMessageListenerValues() throws JMSException {
    	runListenerTest(true);
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return JMSMessageListenerCollectionAspect.aspectOf();
    }

    private void runListenerTest (boolean obscureAttrs) throws JMSException {
        Queue queue = mock(Queue.class);
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
        
        Operation op = getLastEntered();
        
        assertEquals(JMSPluginOperationType.LISTENER_RECEIVE.getOperationType(), op.getType());
        assertEquals(JMSPluginOperationType.LISTENER_RECEIVE.getLabel(), op.getLabel());
        
        JMSPluginUtilsTest.assertHeaders(_mockMessage, op);
        JMSPluginUtilsTest.assertAttributes(msgAttributesMap, op);

        assertObfuscatedValuesState(msgAttributesMap, obscureAttrs);
    }

    private static class MockMessageListener implements MessageListener {
        public MockMessageListener () {
            super();
        }

        public void onMessage(Message msg) {
            // do nothing
        }
    }

}
