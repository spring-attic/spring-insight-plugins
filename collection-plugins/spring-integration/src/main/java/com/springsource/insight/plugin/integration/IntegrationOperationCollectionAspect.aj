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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.springframework.expression.Expression;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.handler.ExpressionEvaluatingMessageProcessor;
import org.springframework.integration.handler.ServiceActivatingHandler;
import org.springframework.integration.splitter.MethodInvokingSplitter;
import org.springframework.integration.transformer.MessageTransformingHandler;
import org.springframework.integration.transformer.Transformer;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.util.ExtraReflectionUtils;
import com.springsource.insight.util.StringUtil;

/**
 * Aspect collecting operations for Spring Integration frames; channels, 
 * message handlers, and transformers are supported.
 *
 */
public aspect IntegrationOperationCollectionAspect extends AbstractIntegrationOperationCollectionAspect {	

	private Map<String, Operation> opCache = new ConcurrentHashMap<String, Operation>();
	private Map<String, MessageHandlerProps> messageHandlerPropsCache = new ConcurrentHashMap<String, MessageHandlerProps>();

	private Field expressionField = ExtraReflectionUtils.getAccessibleField(ExpressionEvaluatingMessageProcessor.class, "expression");
	public static final String DEFAULT_LABEL = "Spring Integration";

	public IntegrationOperationCollectionAspect(){
		super();
	}

	@SuppressAjWarnings
	after (Object object, MethodInvokingSplitter splitter) : 
		execution(public MethodInvokingSplitter.new(Object)) && args(object) && target(splitter) { 

		MessageHandlerProps messageHandlerProps = new MessageHandlerProps(object.getClass().getSimpleName());
		messageHandlerPropsCache.put(getHandlerkey(splitter), messageHandlerProps);
	}

	@SuppressAjWarnings
	after (Object object, Method method, MethodInvokingSplitter splitter) : 
		execution(public MethodInvokingSplitter.new(Object, Method)) && args(object, method) && target(splitter) {

		MessageHandlerProps messageHandlerProps = new MessageHandlerProps(object.getClass().getSimpleName(), method.getName());
		messageHandlerPropsCache.put(getHandlerkey(splitter), messageHandlerProps);
	}

	@SuppressAjWarnings
	after (Object object, String method, MethodInvokingSplitter splitter) : 
		execution(public MethodInvokingSplitter.new(Object, String)) && args(object, method) && target(splitter) {

		MessageHandlerProps messageHandlerProps = new MessageHandlerProps(object.getClass().getSimpleName(), method);
		messageHandlerPropsCache.put(getHandlerkey(splitter), messageHandlerProps);
	}

	@SuppressAjWarnings
	after (Object object, ServiceActivatingHandler handler) : 
		execution(public ServiceActivatingHandler.new(Object)) && args(object) && target(handler)  { 

		MessageHandlerProps serviceActivatingHandlerProps = new MessageHandlerProps(object.getClass().getSimpleName());
		messageHandlerPropsCache.put(getHandlerkey(handler), serviceActivatingHandlerProps);
	}

	@SuppressAjWarnings
	after (Object object, Method method, ServiceActivatingHandler handler) : 
		execution(public ServiceActivatingHandler.new(Object, Method)) && args(object, method) && target(handler) {

		MessageHandlerProps serviceActivatingHandlerProps = new MessageHandlerProps(object.getClass().getSimpleName(), method.getName());
		messageHandlerPropsCache.put(getHandlerkey(handler), serviceActivatingHandlerProps);
	}

	@SuppressAjWarnings
	after (Object object, String method, ServiceActivatingHandler handler) : 
		execution(public ServiceActivatingHandler.new(Object, String)) && args(object, method) && target(handler) {

		MessageHandlerProps serviceActivatingHandlerProps = new MessageHandlerProps(object.getClass().getSimpleName(), method);
		messageHandlerPropsCache.put(getHandlerkey(handler), serviceActivatingHandlerProps);
	}

	@SuppressWarnings("rawtypes")
	@SuppressAjWarnings
	// cant use a specific type in the constructor because it is not consistent across SI versions
	after (ServiceActivatingHandler handler) : execution(public ServiceActivatingHandler.new(*)) && target(handler){		

		Object arg = thisJoinPoint.getArgs()[0];
		if (arg instanceof ExpressionEvaluatingMessageProcessor){
			ExpressionEvaluatingMessageProcessor processor = (ExpressionEvaluatingMessageProcessor)arg;

			String expressionString = "unknown";

			if (expressionField != null){
				Expression expression = ExtraReflectionUtils.getFieldValue(expressionField, processor, Expression.class);
				expressionString = expression.getExpressionString();
			}

			MessageHandlerProps serviceActivatingHandlerProps = 
					new MessageHandlerProps("(expression='" + expressionString + "')");			
			messageHandlerPropsCache.put(getHandlerkey(handler), serviceActivatingHandlerProps);

		}
	}


	@SuppressAjWarnings
	after (Transformer transformer, MessageTransformingHandler transformerHandler) :
		execution(public MessageTransformingHandler.new(Transformer)) && args(transformer) && target(transformerHandler){

		MessageHandlerProps messageHandlerProps = new MessageHandlerProps(transformer.getClass().getSimpleName());
		messageHandlerPropsCache.put(getHandlerkey(transformerHandler), messageHandlerProps);
	}
	
	private String getHandlerkey(Object handler) {
		return String.valueOf(handler.hashCode());
	}

	private class MessageHandlerProps{
		private String methodName;
		private String objectTypeName;
		private String beanString;


		MessageHandlerProps(String objectTypeName){
			this.objectTypeName = objectTypeName;
			setBeanString();
		}

		MessageHandlerProps(String objectTypeName, String methodName){
			this.methodName = methodName;
			this.objectTypeName = objectTypeName;
			setBeanString();
		}
		
		private void setBeanString(){
				int length = objectTypeName.length();

				if (!StringUtil.isEmpty(methodName)) {
					length += 1 + methodName.length();
				}

				StringBuilder builder = new StringBuilder(length);

				builder.append(objectTypeName);

				if (!StringUtil.isEmpty(methodName)) {
					builder.append('#').append(methodName);
				}

				beanString = builder.toString();
		}
	}




	
	public pointcut collectionPoint() : 
		// filter out anonymous channels
		execution (* org.springframework.integration.context.IntegrationObjectSupport+.*(..))
		&&
		(execution(boolean org.springframework.integration.MessageChannel+.send(org.springframework.integration.Message, long))
				|| execution(void org.springframework.integration.core.MessageHandler+.handleMessage(org.springframework.integration.Message))
				|| execution(* org.springframework.integration.transformer.Transformer+.transform(org.springframework.integration.Message)));


	private Operation createCachedOperation(Object target, String beanName) {
		String beanType = target.getClass().getSimpleName();
		OperationType operationType = SpringIntegrationDefinitions.SI_OPERATION_TYPE;
		String generalType = "unknown";
		String label = DEFAULT_LABEL;
		if (target instanceof MessageChannel) {
			generalType = SpringIntegrationDefinitions.CHANNEL;
			label = beanType + "#" + beanName;
			operationType = SpringIntegrationDefinitions.SI_OP_CHANNEL_TYPE;
		} 
		// ServiceActivatingHandler is a special kind of MessageHandler 
		else if (target instanceof ServiceActivatingHandler) {
			generalType = SpringIntegrationDefinitions.SERVICE_ACTIVATOR;
			label = beanType + "#" + beanName;
			operationType = SpringIntegrationDefinitions.SI_OP_SERVICE_ACTIVATOR_TYPE;
		} else if (target instanceof MessageHandler) {
			generalType = SpringIntegrationDefinitions.MESSAGE_HANDLER;
			label = beanType + "#" + beanName;
		} else if (target instanceof Transformer) {
			generalType = SpringIntegrationDefinitions.TRANSFORMER;
			label = beanType + "#" + beanName;
		}
		Operation cachedOp = new Operation()
		.type(operationType)
		.label(label)
		.put(SpringIntegrationDefinitions.SI_COMPONENT_TYPE_ATTR, generalType)
		.put(SpringIntegrationDefinitions.SI_SPECIFIC_TYPE_ATTR, beanType)
		.put(SpringIntegrationDefinitions.BEAN_NAME_ATTR, beanName);
		opCache.put(beanName, cachedOp);
		return cachedOp;
	}

	@SuppressWarnings("synthetic-access")
	@Override
	protected Operation createOperation(JoinPoint jp) {
		Object target = jp.getTarget();
		String beanName = null;

		MessageHandlerProps serviceActivatingHandlerProps = messageHandlerPropsCache.get(getHandlerkey(target));
		if (serviceActivatingHandlerProps != null){
			beanName = serviceActivatingHandlerProps.beanString;
		} else {
			beanName = ((IntegrationObjectSupport) target).getComponentName();
		}

		Operation cachedOp = null;

		if (beanName == null) {
			beanName = "anonymous";			
		} else {
			cachedOp = opCache.get(beanName);
		}

		if (cachedOp == null) {
			cachedOp = createCachedOperation(target, beanName);
		}

		// Payload type can be different on every message
		Message<?> message = (Message<?>) jp.getArgs()[0];
		Class<?> payloadClazz = message.getPayload().getClass();
		String payloadType;
		if (!payloadClazz.isArray()) {
			payloadType = payloadClazz.getName();
		} else {
			payloadType = payloadClazz.getComponentType().getSimpleName() + "[]";
		}

		// The id is different on every message
		MessageHeaders messageHeaders = message.getHeaders();
		UUID id = messageHeaders.getId();
		String idHeader = id.toString();

		Operation op = new Operation().copyPropertiesFrom(cachedOp);		

		op.label(cachedOp.getLabel())
		.type(cachedOp.getType())
		.put(SpringIntegrationDefinitions.PAYLOAD_TYPE_ATTR, payloadType)
		.put(SpringIntegrationDefinitions.ID_HEADER_ATTR, idHeader);
		return op;

	}

	@Override
	public boolean isEndpoint() {
		return true;
	}


}
