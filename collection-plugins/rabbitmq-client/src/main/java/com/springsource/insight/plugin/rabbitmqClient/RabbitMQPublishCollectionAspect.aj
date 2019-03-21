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

package com.springsource.insight.plugin.rabbitmqClient;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.ExceptionUtils;


public aspect RabbitMQPublishCollectionAspect extends AbstractRabbitMQCollectionAspect {
    public RabbitMQPublishCollectionAspect () {
        super(RabbitPluginOperationType.PUBLISH);
    }

    public pointcut publish(String exchange, String routingKey, boolean mandatory, 
                                        boolean immediate, BasicProperties props, byte[] body)
        : execution(void Channel+.basicPublish(String, String, boolean, boolean, BasicProperties,byte[])) 
       && args(exchange,routingKey,mandatory,immediate,props,body)
       && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
        ;
    
    void around(String exchange, String routingKey, boolean mandatory, boolean immediate, BasicProperties props,byte[] body) : 
        publish(exchange,routingKey,mandatory,immediate,props,body) {
        
        final Operation op = createOperation(thisJoinPoint)
        		.label(AbstractRabbitMQResourceAnalyzer.RABBIT + "-" + "Published to " + exchange + "#" + routingKey)
        		.put("exchange", exchange)
				.put("routingKey", routingKey)
				.put("mandatory", mandatory)
				.put("immediate", immediate)
				.put("showPublishData", "true")
				;        
    
        Channel channel = (Channel) thisJoinPoint.getThis();
        Connection conn = channel.getConnection();
        
        if (body != null) {
            op.put("bytes", body.length);
        }
        
        if (conn != null) {
            applyConnectionData(op, conn);
        }
        
        if (props != null) {
            applyPropertiesData(op, props);
        }

        OperationCollector	collector=getCollector();
        collector.enter(op);
        BasicProperties	proceedProps = colorForward(props, op);
        
        try {
            proceed(exchange,routingKey,mandatory,immediate,proceedProps,body);
            collector.exitNormal();
        } catch (Exception e) {
        	collector.exitAbnormal(e);
        	ExceptionUtils.rethrowException(e);
        }
    }
}
