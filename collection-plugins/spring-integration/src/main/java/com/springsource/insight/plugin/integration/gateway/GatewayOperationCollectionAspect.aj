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
package com.springsource.insight.plugin.integration.gateway;

import java.lang.reflect.Method;
import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.integration.gateway.MessagingGatewaySupport;

import com.springsource.insight.collection.OperationCollectionUtil;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.plugin.integration.AbstractIntegrationOperationCollectionAspect;
import com.springsource.insight.plugin.integration.SpringIntegrationDefinitions;
import com.springsource.insight.util.ArrayUtil;

public privileged aspect GatewayOperationCollectionAspect extends AbstractIntegrationOperationCollectionAspect {

    public GatewayOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint():
            execution(void org.springframework.integration.gateway.MessagingGatewaySupport+.send(Object)) ||
                    execution(Object org.springframework.integration.gateway.MessagingGatewaySupport+.receive()) ||
                    execution(Object org.springframework.integration.gateway.MessagingGatewaySupport+.sendAndReceive(Object)) ||
                    execution(Message org.springframework.integration.gateway.MessagingGatewaySupport+.sendAndReceiveMessage(Object));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        return fillOperation(jp, new Operation().type(SpringIntegrationDefinitions.SI_OP_GATEWAY_TYPE)
                .sourceCodeLocation(OperationCollectionUtil.getSourceCodeLocation(jp)));
    }

    @SuppressWarnings("rawtypes")
    private Operation fillOperation(JoinPoint jp, Operation op) {
        MessagingGatewaySupport gateway = (MessagingGatewaySupport) jp.getTarget();

        String beanType = gateway.getClass().getSimpleName();
        String method = jp.getSignature().getName();

        Method proxyMethod = resloveMethod(jp);

        if (proxyMethod != null) {
            beanType = proxyMethod.getDeclaringClass().getSimpleName();
            method = proxyMethod.getName();
        }

        String label = createLabel(beanType, method);

        Object[] args = jp.getArgs();
        if (ArrayUtil.length(args) > 0) {
            Object obj = args[0];
            Object payloadObj = obj;

            Class<?> payloadClass = null;

            if (obj instanceof Message) {
                Message<?> message = (Message<?>) obj;

                MessageHeaders messageHeaders = message.getHeaders();
                UUID id = messageHeaders.getId();
                String idHeader = id.toString();
                op.put(SpringIntegrationDefinitions.ID_HEADER_ATTR, idHeader);
                colorForward(op, messageHeaders);
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

        return op.label(label)
                .put(SpringIntegrationDefinitions.SI_COMPONENT_TYPE_ATTR, SpringIntegrationDefinitions.GATEWAY)
                .put(SpringIntegrationDefinitions.SI_SPECIFIC_TYPE_ATTR, beanType)
                .put(SpringIntegrationDefinitions.BEAN_NAME_ATTR, gateway.getComponentName());
    }

    private static Method resloveMethod(JoinPoint jp) {
        Object gateway = jp.getTarget();
        if (!(gateway instanceof HasRequestMapper)) {
            return null;
        }

        HasRequestMapper hasRequestMapper = (HasRequestMapper) gateway;
        Object mapperInstance = hasRequestMapper.__getRequestMapper();
        if (!(mapperInstance instanceof HasMethod)) {
            return null;
        }

        HasMethod hasMethod = (HasMethod) mapperInstance;
        return hasMethod.__getInsightMethod();
    }

    private static final String createLabel(String beanType, String method) {
        return new StringBuilder(beanType.length() + method.length() + 1)
                .append(beanType)
                .append('#')
                .append(method)
                .toString()
                ;
    }
}