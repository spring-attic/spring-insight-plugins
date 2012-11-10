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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.gateway.MessagingGatewaySupport;
import org.springframework.integration.mapping.InboundMessageMapper;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.ArrayUtil;

public privileged aspect GatewayOperationCollectionAspect extends AbstractIntegrationOperationCollectionAspect {
    
    public GatewayOperationCollectionAspect() {
        super();
    }
    
    /* ------------------------------------------------------------------------------------------------------------- *
     * HasMethod and HasRequestMapper - add support to proxy gateways. Proxy gateways are just interfaces, in-order 
     * to expose the real gateway interface name, and the real method name we must expose:
     * 1. MessagingGatewaySupport#requestMapper - done with HasRequestMapper
     * 2. GatewayMethodInboundMessageMapper#method - done with HasMethod
     * ------------------------------------------------------------------------------------------------------------- */
    interface HasMethod {}
    declare parents: org.springframework.integration.gateway.GatewayMethodInboundMessageMapper implements HasMethod;
    
    private Method HasMethod.__insightMethod;
    public void HasMethod.__setInsightMethod(Method method) { this.__insightMethod = method; }
    public Method HasMethod.__getInsightMethod() { return this.__insightMethod; }
    
    
    interface HasRequestMapper {}
    declare parents: MessagingGatewaySupport implements HasRequestMapper;
    
    @SuppressWarnings("rawtypes")
    private InboundMessageMapper HasRequestMapper.insightMapper;
    
    @SuppressWarnings("rawtypes")
    public void HasRequestMapper.__setInsightMapper(InboundMessageMapper mapper) { this.insightMapper = mapper; }
    
    @SuppressWarnings("rawtypes")
    public InboundMessageMapper HasRequestMapper.__getInsightMapper() { return this.insightMapper; }

    @SuppressWarnings("rawtypes")
    @SuppressAjWarnings
    Object around(Method method, Map map) : call(public org.springframework.integration.gateway.GatewayMethodInboundMessageMapper.new(Method, Map))
                                                    && args(method, map) {
        Object obj = proceed(method, map);
        
        if (obj instanceof HasMethod) {
            ((HasMethod)obj).__setInsightMethod(method);
        }
        
        return obj;
    }
    
    @SuppressAjWarnings
    @SuppressWarnings("rawtypes")
    void around(MessagingGatewaySupport gateway, InboundMessageMapper mapper) : 
                                execution(public void MessagingGatewaySupport.setRequestMapper(InboundMessageMapper))
                                        && args(mapper) && target(gateway) {
        
        if (gateway instanceof HasRequestMapper) {
            ((HasRequestMapper)gateway).__setInsightMapper(mapper);
        }
        
        proceed(gateway, mapper);
    }
    
    /* ------------------------------------------------------------------------------------------------------------- */
    
    public pointcut collectionPoint() : 
        execution(void org.springframework.integration.gateway.MessagingGatewaySupport+.send(Object)) ||
        execution(Object org.springframework.integration.gateway.MessagingGatewaySupport+.receive()) ||
        execution(Object org.springframework.integration.gateway.MessagingGatewaySupport+.sendAndReceive(Object)) ||
        execution(Message org.springframework.integration.gateway.MessagingGatewaySupport+.sendAndReceiveMessage(Object));
    
    @Override
    protected Operation createOperation(JoinPoint jp) {
        
        Operation op = new Operation().type(SpringIntegrationDefinitions.SI_OP_GATEWAY_TYPE);
        
        fillOperation(jp, op);
        return op;
    }
    
    @SuppressWarnings("rawtypes")
    private static void fillOperation(JoinPoint jp, Operation op) {
        MessagingGatewaySupport gateway = (MessagingGatewaySupport)jp.getTarget();
        
        String beanType = gateway.getClass().getSimpleName();
        String method   = jp.getSignature().getName();
        
        Method proxyMethod = resloveMethod(jp);
        
        if (proxyMethod != null) {
            beanType = proxyMethod.getDeclaringClass().getSimpleName();
            method   = proxyMethod.getName();
        }
        
        String label = createLabel(beanType, method);
        
        Object[] args = jp.getArgs();
        if (ArrayUtil.length(args) > 0) {
            Object obj = args[0];
            Object payloadObj = obj;
            
            Class<?> payloadClass = null;
            
            if (obj instanceof Message) {
            	Message message = (Message)obj;
            	
        		MessageHeaders messageHeaders = message.getHeaders();
        		UUID id = messageHeaders.getId();
        		String idHeader = id.toString();
            	op.put(SpringIntegrationDefinitions.ID_HEADER_ATTR, idHeader);
            	
				payloadObj = message.getPayload();
                
                if (payloadObj != null) {
                    payloadClass = payloadObj.getClass();
                }
            } else if (proxyMethod != null) {
                Class[] parameterTypes = proxyMethod.getParameterTypes();
                payloadClass = ArrayUtil.length(parameterTypes) > 0 ? parameterTypes[0] : null;
            } else if (obj != null) {
                payloadClass = obj.getClass();
            }
            
            if (payloadClass != null) {
                op.put(SpringIntegrationDefinitions.PAYLOAD_TYPE_ATTR, payloadClass.getName());
            }
        }
        
        op.label(label)
          .put(SpringIntegrationDefinitions.SI_COMPONENT_TYPE_ATTR, SpringIntegrationDefinitions.GATEWAY)
          .put(SpringIntegrationDefinitions.SI_SPECIFIC_TYPE_ATTR, beanType)
          .put(SpringIntegrationDefinitions.BEAN_NAME_ATTR,  gateway.getComponentName());
    }

    
    @SuppressWarnings("rawtypes")
    private static Method resloveMethod(JoinPoint jp) {
        Object gateway = jp.getTarget();
        
        if (!(gateway instanceof HasRequestMapper)) {
            return null;
        }
        
        HasRequestMapper hasRequestMapper = (HasRequestMapper) gateway;
        InboundMessageMapper mapper = hasRequestMapper.__getInsightMapper();
        
        if (!(mapper instanceof HasMethod)) {
            return null;
        }
        
        HasMethod hasMethod = (HasMethod) mapper;
        
        return hasMethod.__getInsightMethod();
    }
    
    private static final String createLabel(String beanType, String method) {
        StringBuilder builder = new StringBuilder(beanType.length() + method.length() + 1);
        
        builder.append(beanType);
        builder.append('#');
        builder.append(method);
        
        return builder.toString();
    }
    
    @Override
	public boolean isEndpoint() {
		return true;
	}
}