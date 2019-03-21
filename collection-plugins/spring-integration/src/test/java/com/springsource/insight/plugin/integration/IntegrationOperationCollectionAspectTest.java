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

package com.springsource.insight.plugin.integration;

import java.util.UUID;

import org.junit.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.integration.transformer.Transformer;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

/**
 * This test verifies that Integration operations correctly captured by
 * the aspect, {@link IntegrationOperationCollectionAspect}.
 *
 * @author Gary Russell
 */
public class IntegrationOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
    public IntegrationOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testChannelCollected() {
        MyChannel channel = new MyChannel();
        channel.setBeanName("testChannel");
        Message<String> message = new GenericMessage<String>("Test");
        channel.send(message);
        assertIntegrationOperation("Channel", "testChannel", message, SpringIntegrationDefinitions.SI_OP_CHANNEL_TYPE);
    }

    @Test
    public void testHandlerCollected() {
        MyHandler handler = new MyHandler();
        handler.setBeanName("testHandler");
        Message<String> message = new GenericMessage<String>("Test");
        handler.handleMessage(message);
        assertIntegrationOperation("MessageHandler", "testHandler", message, SpringIntegrationDefinitions.SI_OPERATION_TYPE);
    }

    @Test
    public void testTransformerCollected() {
        MyTransformer transformer = new MyTransformer();
        transformer.setBeanName("testTransform");
        transformer.transformedMessage = new GenericMessage<String>("Transformed");
        Message<String> message = new GenericMessage<String>("Test");
        Message<?> result = transformer.transform(message);
        assertSame("Mismatched transformed instance", transformer.transformedMessage, result);
        assertIntegrationOperation("Transformer", "testTransform", message, SpringIntegrationDefinitions.SI_OPERATION_TYPE);
    }

    private Operation assertIntegrationOperation(String compType, String beanName, Message<String> message, OperationType type) {
        Operation op = getLastEntered();
        assertNotNull("No operation", op);
        assertEquals("Mismatched type", type, op.getType());

        assertEquals("Mismatched component type", compType, op.get("siComponentType", String.class));
        assertEquals("Mismatched bean name", beanName, op.get("beanName", String.class));

        MessageHeaders hdrs = message.getHeaders();
        UUID msgId = hdrs.getId();
        assertEquals("Mismatched message id", msgId.toString(), op.get("idHeader", String.class));
        assertEquals("Mismatched payload type", "java.lang.String", op.get("payloadType", String.class));

        return op;
    }

    @Override
    public IntegrationOperationCollectionAspect getAspect() {
        return IntegrationOperationCollectionAspect.aspectOf();
    }

    private static class MyChannel extends IntegrationObjectSupport implements MessageChannel {
        public MyChannel() {
            super();
        }

        public boolean send(Message<?> message) {
            return this.send(message, -1);
        }

        public boolean send(Message<?> message, long timeout) {
            return false;
        }
    }

    private static class MyHandler extends IntegrationObjectSupport implements MessageHandler {
        public MyHandler() {
            super();
        }

        public void handleMessage(Message<?> message) throws MessagingException {
            // do nothing
        }
    }

    private static class MyTransformer extends IntegrationObjectSupport implements Transformer {
        Message<String> transformedMessage;

        public MyTransformer() {
            super();
        }

        public Message<?> transform(Message<?> message) {
            return transformedMessage;
        }
    }
}
