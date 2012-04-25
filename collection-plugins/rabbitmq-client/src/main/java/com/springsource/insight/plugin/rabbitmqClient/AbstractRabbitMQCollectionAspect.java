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

import java.net.InetAddress;
import java.util.Map;
import java.util.Map.Entry;

import org.aspectj.lang.JoinPoint;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.impl.AMQConnection;
import com.rabbitmq.client.impl.LongString;
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.strategies.BasicCollectionAspectProperties;
import com.springsource.insight.collection.strategies.CollectionAspectProperties;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;

public abstract class AbstractRabbitMQCollectionAspect extends OperationCollectionAspectSupport {

    protected void applyPropertiesData(Operation op, BasicProperties props) {
        OperationMap map = op.createMap("props");

        map.putAnyNonEmpty("Type", props.getType());
        map.putAnyNonEmpty("App Id", props.getAppId());
        map.putAnyNonEmpty("User Id", props.getUserId());
        map.put("Class Id", props.getClassId());
        map.putAnyNonEmpty("Reply To", props.getReplyTo());
        map.putAnyNonEmpty("Priority", props.getPriority());
        map.putAnyNonEmpty("Class Name", props.getClassName());
        map.putAnyNonEmpty("Timestamp", props.getTimestamp());
        map.putAnyNonEmpty("Message Id", props.getMessageId());
        map.putAnyNonEmpty("Expiration", props.getExpiration());
        map.putAnyNonEmpty("Content Type", props.getContentType());
        map.putAnyNonEmpty("Delivery Mode", props.getDeliveryMode());
        map.putAnyNonEmpty("Correlation Id", props.getCorrelationId());
        map.putAnyNonEmpty("Content Encoding", props.getContentEncoding());

        Map<String, Object> headers = props.getHeaders();

        if (headers != null) {
            OperationMap headersMap = op.createMap("headers");

            for (Entry<String, Object> entry : headers.entrySet()) {
                Object value = entry.getValue();
                
                if (value instanceof LongString) {
                    byte[] bytes = ((LongString) value).getBytes();
                    value = new String(bytes);
                }
                
                headersMap.putAnyNonEmpty(entry.getKey(), value);
            }
        }
    }

    protected void applyConnectionData(Operation op, Connection conn) {
        String connectionUrl = null;
        
        if (conn instanceof AMQConnection) {
            connectionUrl = conn.toString();
        } else {
            InetAddress address = conn.getAddress();
            int port = conn.getPort();
            
            StringBuilder sb = new StringBuilder("amqp://");
            sb.append(address.getHostAddress()).append(":").append(port);
            
            connectionUrl = sb.toString();
        }
        
        op.put("host", conn.getAddress().getHostAddress());
        op.put("port", conn.getPort());
        op.put("connectionUrl", connectionUrl);
        
        //try to extract server version
        String version = getVersion(conn.getServerProperties());
        op.put("serverVersion", version);
        
        //try to extract client version
        version = getVersion(conn.getClientProperties());
        op.put("clientVersion", version);
    }

    private String getVersion(Map<String, Object> properties) {
        String version = null;
        
        if (properties != null) {
            Object obj = properties.get("version");
            
            if (obj != null) {
                version = String.valueOf(obj); 
            }
        }
        
        return version;
    }

}
