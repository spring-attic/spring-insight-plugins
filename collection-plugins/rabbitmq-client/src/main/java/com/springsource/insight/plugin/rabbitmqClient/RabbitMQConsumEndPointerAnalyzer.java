/**
 * Copyright 2009-2010 the original author or authors.
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

package com.springsource.insight.plugin.rabbitmqClient;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;

public class RabbitMQConsumEndPointerAnalyzer extends AbstractRabbitMQEndPointAnalyzer {
    private static final String PREFIX = "RabbitMQ Consume from: ";
    private static final OperationType TYPE = OperationType.valueOf("rabbitmq-client-consumer");
    
    @Override
    protected String getRoutingKey(Operation op) {
        OperationMap envelopeMap = op.get("envelope", OperationMap.class);
        return envelopeMap.get("routingKey", String.class);
    }
    
    @Override
    protected String getExchange(Operation op) {
        OperationMap envelopeMap = op.get("envelope", OperationMap.class);
        return envelopeMap.get("exchange", String.class);
    }
    
    @Override
    protected String getExamplePrefix() {
        return PREFIX;
    }

    @Override
    protected OperationType getOperationType() {
        return TYPE;
    }

}