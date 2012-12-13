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

package com.springsource.insight.plugin.integration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.util.StringUtils;

import com.springsource.insight.collection.TrailingAbstractOperationCollectionAspect;
import com.springsource.insight.intercept.color.ColorManager.ColorParams;
import com.springsource.insight.intercept.operation.Operation;


public privileged aspect MessageListenerOperationCollectionAspect extends TrailingAbstractOperationCollectionAspect {

	public MessageListenerOperationCollectionAspect(){
		super();
	}
	
    @Override
    public final String getPluginName() {
        return IntegrationPluginRuntimeDescriptor.PLUGIN_NAME;
    }
	
	public pointcut collectionPoint() : 
		execution (void MessageListener+.onMessage(Message));

	@SuppressWarnings("synthetic-access")
	@Override
	protected Operation createOperation(JoinPoint jp) {		
		MessageListener messageListener = (MessageListener) jp.getTarget();

		MessageListenerProps cachedMessageListenerProps = messageListenerPropsCache.get(SpringIntegrationDefinitions.getObjectKey(messageListener));
		
		// we only want to catch MessageListener+.onMessage calls of MessageListeners that belong to an AmqpInboundChannelAdapter
		if (cachedMessageListenerProps == null){
			return null;
		}

		String beanName = cachedMessageListenerProps.adapterBeanName;
		String beanType = cachedMessageListenerProps.adapterBeanType;       
		String label = beanType + "#" + beanName;			
		Message message = (Message) jp.getArgs()[0];		
		final MessageProperties	props= message.getMessageProperties();
		final Operation op = new Operation()
						.type(SpringIntegrationDefinitions.SI_OP_MESSAGE_ADAPTER_TYPE)
						.label(label)
						.put("listeningOnQueues", cachedMessageListenerProps.queueNames)  
						.put("messageExchange", message.getMessageProperties().getReceivedExchange())
						.put("messageRoutingKey", message.getMessageProperties().getReceivedRoutingKey())
						.put("messageContentType", message.getMessageProperties().getContentType())
						.put("outputChannel", cachedMessageListenerProps.outputChannelName) 
						.put("siComponentType", SpringIntegrationDefinitions.MESSAGE_ADAPTER)
						.put("siSpecificType", beanType)
						.put("beanName", beanName);
		colorForward(new ColorParams() {
				public void setColor(String key, String value) {
					if (props == null)
						return;	// debug breakpoint
					props.setHeader(key, value);
				}

				public Operation getOperation() {
					return op;
				}
				
			});
		return op;
	}
	
	
	//We want to catch all MessageListener+.onMessage calls, but only if they belong to an AmqpInboundChannelAdapter
	//Here we use an aspect to remember the AmqpInboundChannelAdapter for each message listener so we have access 
	//to it when a message is actually consumed
	
	//This is the description of the AmqpInboundChannelAdapter class:
	//"Adapter that receives Messages from an AMQP Queue, converts them into
	//Spring Integration Messages, and sends the results to a Message Channel"

	private Map<String, MessageListenerProps> messageListenerPropsCache = new ConcurrentHashMap<String, MessageListenerProps>();

	@SuppressAjWarnings
	after() : execution(void AmqpInboundChannelAdapter.onInit()){
			AmqpInboundChannelAdapter inboundChannelAdapter = (AmqpInboundChannelAdapter) thisJoinPoint.getThis();
			MessageListenerProps cachedMessageListenerProps = createCachedMessageListenerProps(inboundChannelAdapter);
			messageListenerPropsCache.put(
					SpringIntegrationDefinitions.getObjectKey(inboundChannelAdapter.messageListenerContainer.getMessageListener()),
					cachedMessageListenerProps);
	}

	private MessageListenerProps createCachedMessageListenerProps(AmqpInboundChannelAdapter inboundChannelAdapter) {
		String outputChannelName = inboundChannelAdapter.outputChannel.toString();
		String queueNames = StringUtils.arrayToCommaDelimitedString(inboundChannelAdapter.messageListenerContainer.getQueueNames());
		String beanName = inboundChannelAdapter.getComponentName();
		String beanType = inboundChannelAdapter.getClass().getSimpleName();

		MessageListenerProps messageListenerProps = new MessageListenerProps(outputChannelName, queueNames, beanName, beanType);
		return messageListenerProps;
	}

	private class MessageListenerProps{
		private String outputChannelName;
		private String queueNames;
		private String adapterBeanName;
		private String adapterBeanType;

		MessageListenerProps(String outChannelName, String queueNamesList, String assignedAdapterBeanName, String assignedAdapterBeanType){
			outputChannelName = outChannelName;
			queueNames = queueNamesList;
			adapterBeanName = assignedAdapterBeanName;
			adapterBeanType = assignedAdapterBeanType;			 
		}
	}

}
