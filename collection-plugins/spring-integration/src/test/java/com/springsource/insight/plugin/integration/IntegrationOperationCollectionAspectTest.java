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

package com.springsource.insight.plugin.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.mockito.MockitoAnnotations.initMocks;

import javax.jms.JMSException;
import javax.jms.Session;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessagingException;
import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.message.GenericMessage;
import org.springframework.integration.transformer.Transformer;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.plugin.integration.IntegrationOperationCollectionAspect;

/**
 * This test verifies that Integration operations correctly captured by
 * the aspect, {@link IntegrationOperationCollectionAspect}.
 * 
 * @author Gary Russell
 */
public class IntegrationOperationCollectionAspectTest
    extends OperationCollectionAspectTestSupport
{
    @Mock
    private javax.jms.Message mockMessage;
    
    private Message<String> transformedMessage;
    
    @Before
    public void init() {
        initMocks(this);
    }
    
    @Test
    public void channelCollected() {
        MyChannel channel = new MyChannel();
        channel.setBeanName("testChannel");
        Message<String> message = new GenericMessage<String>("Test");
        channel.send(message);
        
        Operation op = getLastEntered(Operation.class);
        
        assertEquals("Channel", op.get("siComponentType"));
        assertEquals("testChannel", op.get("beanName"));
        assertEquals(message.getHeaders().getId().toString(), op.get("idHeader"));
        assertEquals("java.lang.String", op.get("payloadType"));
    }

    @Test
    public void handlerCollected() {
        MyHandler handler = new MyHandler();
        handler.setBeanName("testHandler");
        Message<String> message = new GenericMessage<String>("Test");
        handler.handleMessage(message);
        
        Operation op = getLastEntered(Operation.class);
        
        assertEquals("MessageHandler", op.get("siComponentType"));        
        assertEquals("testHandler", op.get("beanName"));
        assertEquals(message.getHeaders().getId().toString(), op.get("idHeader"));
        assertEquals("java.lang.String", op.get("payloadType"));
    }

    @Test
    public void transformerCollected() {
        MyTransformer transformer = new MyTransformer();
        transformer.setBeanName("testTransform");
        Message<String> message = new GenericMessage<String>("Test");
        this.transformedMessage = new GenericMessage<String>("Transformed");
        transformer.transform(message);
        
        Operation op = getLastEntered(Operation.class);
        
        assertEquals("Transformer", op.get("siComponentType"));        
        assertEquals("testTransform", op.get("beanName"));
        assertEquals(message.getHeaders().getId().toString(), op.get("idHeader"));
        assertEquals("java.lang.String", op.get("payloadType"));
    }

    private class MyChannel extends IntegrationObjectSupport implements MessageChannel {

        public boolean send(Message<?> message) {
            return this.send(message, -1);
        }

        public boolean send(Message<?> message, long timeout) {
            return false;
        }
        
    }
    
    private class MyHandler extends IntegrationObjectSupport implements MessageHandler {

        public void handleMessage(Message<?> message) throws MessagingException {
        }
        
    }
    
    private class MyTransformer extends IntegrationObjectSupport implements Transformer {

        public Message<?> transform(Message<?> message) {
            return transformedMessage;
        }
        
    }

    
    @Override
    public OperationCollectionAspectSupport getAspect() {
        return IntegrationOperationCollectionAspect.aspectOf();
    }
}
