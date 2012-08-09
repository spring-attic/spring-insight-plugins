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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.impl.AMQConnection;
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.intercept.color.ColorManager.ColorParams;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.util.ClassUtil;
import com.springsource.insight.util.ExtraReflectionUtils;
import com.springsource.insight.util.MapUtil;
import com.springsource.insight.util.ReflectionUtils;

public abstract class AbstractRabbitMQCollectionAspect extends OperationCollectionAspectSupport {
    private final Field messageHeaders = ExtraReflectionUtils.getAccessibleField(BasicProperties.class, "headers");
    private static Method getBytesMethod;
    
    @SuppressWarnings("rawtypes")
	private static Class longStringClass;
    
    private static final Logger  _logger = Logger.getLogger(AbstractRabbitMQCollectionAspect.class.getName());
    
    protected AbstractRabbitMQCollectionAspect () {
    	super();
    	setLongStringClass();
    }

    private void setLongStringClass() {
    	ClassLoader cl = ClassUtil.getDefaultClassLoader(getClass());
    	
    	try {
			longStringClass = ClassUtil.loadClassByName(cl, "com.rabbitmq.client.impl.LongString");
		} catch (ClassNotFoundException e) {
			try {
				longStringClass = ClassUtil.loadClassByName(cl, "com.rabbitmq.client.LongString");
			} catch (ClassNotFoundException e1) {
				_logger.warning("Cannot find LongString class from amqp-client jar");				
			}
		}
    	
    	if (longStringClass != null) {
			getBytesMethod = ExtraReflectionUtils.getAccessibleMethod(longStringClass, "getBytes");
		} else {
			getBytesMethod = null;
		}
		
	}

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
                
                if (longStringClass != null && value!=null && value.getClass().isAssignableFrom(longStringClass)) {
                	byte[] bytes = null;
                	
					try {
						bytes = (byte[]) getBytesMethod.invoke(value);
					} catch (Exception e) {
						if (_logger.isLoggable(Level.FINE)) {
							_logger.log(Level.FINE, "couldn't get getBytes from LongString", e);
						}
					}
					
					if (bytes != null) {
						value = new String(bytes);
						headersMap.putAnyNonEmpty(entry.getKey(), value);
					}
                }
                
            }
        }
    }
    
    

    protected void applyConnectionData(Operation op, Connection conn) {
        InetAddress	address = conn.getAddress();
        String		host = address.getHostAddress();
        int			port = conn.getPort();
        final String connectionUrl;
        if (conn instanceof AMQConnection) {
            connectionUrl = conn.toString();
        } else {
            connectionUrl = "amqp://" + host + ":" + port; 
        }
        
        op.put("host", host);
        op.put("port", port);
        op.put("connectionUrl", connectionUrl);
        
        //try to extract server version
        String serverVersion = getVersion(conn.getServerProperties());
        op.putAnyNonEmpty("serverVersion", serverVersion);
        
        //try to extract client version
        String	clientVersion = getVersion(conn.getClientProperties());
        op.putAnyNonEmpty("clientVersion", clientVersion);
    }

    static String getVersion(Map<String,?> properties) {
        if (MapUtil.size(properties) <= 0) {
        	return null;
        }
        
        Object obj = properties.get("version");
        if (obj != null) {
        	return String.valueOf(obj); 
        } else {
        	return null;
        }
    }
    
    protected void colorForward(BasicProperties props, final Operation op) {
        try {
            final Map<String, Object> map = new HashMap<String, Object>();
            
            if (props != null) {
                Map<String, Object> old = props.getHeaders();
                if (MapUtil.size(old) > 0) {
                    map.putAll(old);
                }
                
                colorForward(new ColorParams() {
                    public void setColor(String key, String value) {
                        map.put(key, value);
                    }
                    
                    public Operation getOperation() {
                        return op;
                    }
                });
                
                ReflectionUtils.setField(messageHeaders, props, Collections.unmodifiableMap(map));
            }
        } catch (Exception e) {
            //nothing to do...
        }
    }

}
