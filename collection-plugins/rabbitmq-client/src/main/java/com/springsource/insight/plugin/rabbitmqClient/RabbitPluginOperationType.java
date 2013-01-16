
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

package com.springsource.insight.plugin.rabbitmqClient;

import com.springsource.insight.intercept.operation.OperationType;

enum RabbitPluginOperationType {
    CONSUME("rabbitmq-client-consumer", "Consume", "RabbitMQ Consume from: "),
    PUBLISH("rabbitmq-client-publish", "Publish", "RabbitMQ Publish to: ");

    
    private OperationType operationType;
    private String label;
    private String endPointPrefix;
    
    private RabbitPluginOperationType(String operationTypeName, String label, String endPointPrefix) {
        this.operationType = OperationType.valueOf(operationTypeName);
        this.label = label;
        this.endPointPrefix = endPointPrefix;
    }
    
    public OperationType getOperationType() {
        return operationType;
    }
    
    public String getLabel() {
        return label;
    }
    
    public String getEndPointPrefix() {
        return endPointPrefix;
    }

    public static RabbitPluginOperationType getType(OperationType type) {
        for(RabbitPluginOperationType otype : RabbitPluginOperationType.values()) {
            if (otype.operationType == type) {
                return otype;
            }
        }
        return null;
    }
}
