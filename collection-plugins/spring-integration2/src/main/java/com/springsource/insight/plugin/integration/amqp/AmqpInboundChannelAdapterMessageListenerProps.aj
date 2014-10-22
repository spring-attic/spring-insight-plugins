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

package com.springsource.insight.plugin.integration.amqp;

import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.util.StringUtils;

import com.springsource.insight.plugin.integration.SpringIntegrationDefinitions;
import com.springsource.insight.plugin.integration.MessageListenerProps;

/**
 *
 * We want to catch all MessageListener+.onMessage calls, but only if they belong
 * to an AmqpInboundChannelAdapter. Here we use an aspect to remember the
 * AmqpInboundChannelAdapter for each message listener so we have access to it 
 * when a message is actually consumed
 *
 * This is the description of the AmqpInboundChannelAdapter class:
 *
 * &quot;Adapter that receives Messages from an AMQP Queue, converts them into
 * Spring Integration Messages, and sends the results to a Message Channel&quot;
 */
public privileged aspect AmqpInboundChannelAdapterMessageListenerProps {
    public AmqpInboundChannelAdapterMessageListenerProps() {
        super();
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after(): execution(void AmqpInboundChannelAdapter.onInit())
            {
                AmqpInboundChannelAdapter inboundChannelAdapter = (AmqpInboundChannelAdapter) thisJoinPoint.getThis();
                MessageListenerProps cachedMessageListenerProps = createCachedMessageListenerProps(inboundChannelAdapter);
                MessageListenerProps.putMessageListenerProps(
                        SpringIntegrationDefinitions.getObjectKey(inboundChannelAdapter.messageListenerContainer.getMessageListener()),
                        cachedMessageListenerProps);
            }

    static MessageListenerProps createCachedMessageListenerProps(AmqpInboundChannelAdapter inboundChannelAdapter) {
        String outputChannelName = String.valueOf(inboundChannelAdapter.outputChannel);
        String queueNames = StringUtils.arrayToCommaDelimitedString(inboundChannelAdapter.messageListenerContainer.getQueueNames());
        String beanName = inboundChannelAdapter.getComponentName();
        String beanType = inboundChannelAdapter.getClass().getSimpleName();

        return new MessageListenerProps(outputChannelName, queueNames, beanName, beanType);
    }
}
