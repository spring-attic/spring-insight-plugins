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

import org.aspectj.lang.JoinPoint;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;

import com.springsource.insight.collection.OperationCollectionUtil;
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

	@Override
	protected Operation createOperation(JoinPoint jp) {		
		MessageListener messageListener = (MessageListener) jp.getTarget();

		MessageListenerProps cachedMessageListenerProps =
				MessageListenerProps.getMessageListenerProps(SpringIntegrationDefinitions.getObjectKey(messageListener));
		// we only want to catch MessageListener+.onMessage calls of MessageListeners that belong (e.g.) to an AmqpInboundChannelAdapter
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
						.sourceCodeLocation(OperationCollectionUtil.getSourceCodeLocation(jp))
						.put("listeningOnQueues", cachedMessageListenerProps.queueNames)  
						.putAnyNonEmpty("messageExchange", (props == null) ? null : props.getReceivedExchange())
						.putAnyNonEmpty("messageRoutingKey", (props == null) ? null :  props.getReceivedRoutingKey())
						.putAnyNonEmpty("messageContentType", (props == null) ? null :  props.getContentType())
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
}
