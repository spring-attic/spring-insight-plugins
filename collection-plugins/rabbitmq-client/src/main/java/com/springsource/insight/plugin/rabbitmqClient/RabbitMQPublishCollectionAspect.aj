/**
 * Copyright 2009-2011 the original author or authors.
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

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.springsource.insight.intercept.operation.Operation;


public aspect RabbitMQPublishCollectionAspect extends AbstractRabbitMQCollectionAspect {

    public pointcut publish(String exchange, String routingKey, boolean mandatory, 
                                        boolean immediate, BasicProperties props, byte[] body)
        : execution(void Channel+.basicPublish(String, String, boolean, boolean, BasicProperties,byte[])) 
                                                   && args(exchange,routingKey,mandatory,immediate,props,body);
    
    before(String exchange, String routingKey, boolean mandatory,
             boolean immediate, BasicProperties props,byte[] body) :
                 publish(exchange,routingKey,mandatory,immediate,props,body) {
        
        Operation op = new Operation()
                            .type(RabbitPluginOperationType.PUBLISH.getOperationType())
                            .label(RabbitPluginOperationType.PUBLISH.getLabel());
        
        op.put("exchange", exchange);
        op.put("routingKey", routingKey);
        op.put("mandatory", mandatory);
        op.put("immediate", immediate);        
        
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
        
        getCollector().enter(op);
    }
    
    after(String exchange, String routingKey, boolean mandatory,
            boolean immediate, BasicProperties props,byte[] body) returning() :
                publish(exchange,routingKey,mandatory,immediate,props,body) {
                
        getCollector().exitNormal();
    }
            
    after(String exchange, String routingKey, boolean mandatory,
            boolean immediate, BasicProperties props,byte[] body) throwing(Throwable t) :
                publish(exchange,routingKey,mandatory,immediate,props,body) {
                
        getCollector().exitAbnormal(t);
    }
}
