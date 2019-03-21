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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aspectj.lang.JoinPoint;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.impl.AMQConnection;
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionUtil;
import com.springsource.insight.intercept.color.ColorManager.ColorParams;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.ClassUtil;
import com.springsource.insight.util.ExtraReflectionUtils;
import com.springsource.insight.util.MapUtil;
import com.springsource.insight.util.ReflectionUtils;

public abstract class AbstractRabbitMQCollectionAspect extends OperationCollectionAspectSupport {
    private static final AtomicReference<Field> messageHeadersField=new AtomicReference<Field>(null);
    private static final AtomicReference<Class<?>>	longStringClassHolder=new AtomicReference<Class<?>>(null);
    private static final AtomicReference<Object>	bytesMethodHolder=new AtomicReference<Object>(null);

    static final List<String>	LONG_STRING_CLASSES=
    		Collections.unmodifiableList(
    				Arrays.asList(
    						"com.rabbitmq.client.impl.LongString",
    						"com.rabbitmq.client.LongString"));

    protected final Logger  _logger = Logger.getLogger(getClass().getName());
    protected final RabbitPluginOperationType	pluginOpType;
    protected AbstractRabbitMQCollectionAspect (RabbitPluginOperationType rabbitOpType) {
    	if ((pluginOpType=rabbitOpType) == null) {
    		throw new IllegalStateException("No plugin operation type specified");
    	}
    }

    @Override
	public String getPluginName() {
    	return RabbitMQPluginRuntimeDescriptor.PLUGIN_NAME;
   	}

    protected Operation createOperation(JoinPoint jp) {
        return new Operation()
                    .type(pluginOpType.getOperationType())
                    .label(pluginOpType.getLabel())
                    .sourceCodeLocation(OperationCollectionUtil.getSourceCodeLocation(jp))
                    ;
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

        updateHeadersMap(op, props.getHeaders());
    }

	protected OperationMap updateHeadersMap (Operation op, Map<String,?> headers) {
        return updateHeadersMap(op.createMap("headers"), headers);
	}

	protected OperationMap updateHeadersMap (OperationMap headersMap, Map<String,?> headers) {
        if (MapUtil.size(headers) <= 0) {
        	return headersMap;
        }

        Class<?> longStringClass=getLongStringClass(getClass());
        if (longStringClass == null) {
        	return headersMap;
        }

        Method	bytesMethod=getBytesRetrievalMethod(getClass());
        if (bytesMethod == null) {
        	return headersMap;
        }

        for (Map.Entry<String, ?> entry : headers.entrySet()) {
        	String	key = entry.getKey();
        	Object 	value = entry.getValue();
        	if (value == null) {
        		continue;
        	}

        	Class<?>	valueType = value.getClass();
        	if (!valueType.isAssignableFrom(longStringClass)) {
        		continue;
        	}

        	final byte[] bytes;
        	try {
        		bytes = (byte[]) bytesMethod.invoke(value);
        		if (ArrayUtil.length(bytes) <= 0) {
        			continue;
        		}
        	} catch (Exception e) {
        		if (_logger.isLoggable(Level.FINE)) {
        			_logger.fine("Failed (" + e.getClass().getSimpleName() + ")"
        					+ " to get bytes of " + valueType.getName()
        					+ " instance for key=" + key + ": " + e.getMessage());
        		}
        		continue;
        	}

        	headersMap.put(key, new String(bytes));
        }

        return headersMap;
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
    
    protected BasicProperties colorForward(BasicProperties orgProps, final Operation op) {
    	Field	headersField=getMessageHeadersField();
    	BasicProperties	props=orgProps;
    	if (headersField == null) {
    		return props;
    	}

        try {
            final Map<String, Object> map = new HashMap<String, Object>();
            Map<String, Object> old = (props != null) ? props.getHeaders() : null;
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
            
            if (props == null) {
                BasicProperties.Builder builder = new BasicProperties.Builder();
                props = builder.build();
            }
            
            Map<String, Object>	hdrsMap=Collections.unmodifiableMap(map);
            ReflectionUtils.setField(headersField, props, hdrsMap);
            return props;
        } catch (Exception e) {
            if (_logger.isLoggable(Level.FINE)) {
            	_logger.fine("colorForward(" + op + ")"
            			   + " failed (" + e.getClass().getSimpleName() + ")"
	            		   + " to append color: " + e.getMessage());
            }

    		return props;
        }
    }

    static Field getMessageHeadersField () {
    	// kind of a D.C.L. but works...
    	Field	field=messageHeadersField.get();
    	if (field == null) {
    		if ((field=ExtraReflectionUtils.getAccessibleField(BasicProperties.class, "headers")) != null) {
    			messageHeadersField.set(field);
    		}
    	}

    	return field;
    }

    static Method getBytesRetrievalMethod (Class<?> anchor) {
    	Object		method=bytesMethodHolder.get();
    	if (method != null) {
    		if (method instanceof Boolean) {
    			return null;
    		} else {
    			return (Method) method;
    		}
    	}

    	Class<?>	longStringClass=getLongStringClass(anchor);
    	if (longStringClass == null) {
    		return null;
    	}
    	
        if ((method=ExtraReflectionUtils.getAccessibleMethod(longStringClass, "getBytes")) == null) {
    		Logger	LOG=Logger.getLogger(anchor.getName());
    		LOG.warning("getBytesRetrievalMethod(" + anchor.getSimpleName() + ") no match found");
    		bytesMethodHolder.set(Boolean.FALSE);	// avoid repeated calls
    		return null;
        }

        bytesMethodHolder.set(method);
        return (Method) method;
    }

    static Class<?> getLongStringClass(Class<?> anchor) {
    	Class<?>	clazz=longStringClassHolder.get();
    	if (clazz != null) {
    		// check if placeholder used
        	if (clazz == String.class) {
        		return null;
        	} else {
        		return clazz;
        	}
    	}

    	ClassLoader cl = ClassUtil.getDefaultClassLoader(anchor);
    	for (String	className : LONG_STRING_CLASSES) {
        	try {
    			if ((clazz=ClassUtil.loadClassByName(cl, className)) == null) {
    				throw new IllegalStateException("Failed to load present class");
    			}

    			longStringClassHolder.set(clazz);
    			return clazz;
        	} catch(Exception e) {
        		if (!(e instanceof ClassNotFoundException)) {
        			Logger	LOG=Logger.getLogger(anchor.getName());
        			LOG.warning("Failed (" + e.getClass().getSimpleName() + ")"
        				      + " to load class=" + className
        				      + ": " + e.getMessage()); 
        		}
        	}
    	}

		Logger	LOG=Logger.getLogger(anchor.getName());
		LOG.warning("getLongStringClass(" + anchor.getSimpleName() + ") no match found");
		longStringClassHolder.set(String.class);	// avoid repeated load attempts and use an incompatible class
		return null;
	}
    
    @Override
    public boolean isMetricsGenerator(){
        return true; // This provides an endpoint and external resource
    }
}
