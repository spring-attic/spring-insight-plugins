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
package com.springsource.insight.plugin.redis;

import org.aspectj.lang.JoinPoint;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

/**
 * Aspect class for RedisMap operation
 */
public aspect RedisConnectionFactoryOperationCollectionAspect extends AbstractOperationCollectionAspect {

    protected static final OperationType TYPE = OperationType.valueOf("redis-connection-factory");

    // TODO - change pointcut name back to getConnection to get this working
    // pending on the ability to delete the operation 
    // This aspect will create a sub frame for every operation on map/list which is too much.
    // On the other hand we have to instrument this because this is the only place where
    // we are guaranteed to have access to the connection
    public pointcut redisGetConnection()
        : execution(* RedisConnectionFactory.getConnectionnnn(..));

    public pointcut collectionPoint() : redisGetConnection();

    @Override
    protected Operation createOperation(JoinPoint jp) {
        String method = jp.getSignature().getName();
        Operation op = new Operation()
            .type(TYPE)
            .put("method", method);
        RedisConnectionFactory redisConnectionFactory = (RedisConnectionFactory)jp.getTarget();
        op.label("RedisConnection: " + redisConnectionFactory.getClass().getSimpleName());
        // TODO - this is by reflection because although all the 3 connection factories have these methods 
        // they do not appear in the interface
        // SO - better to create 3 separate aspects
        try {
			op.put("host", redisConnectionFactory.getClass().getMethod("getHostName").invoke(redisConnectionFactory).toString());
			op.put("port", Integer.parseInt(redisConnectionFactory.getClass().getMethod("getPort").invoke(redisConnectionFactory).toString()));
			op.put("dbName", redisConnectionFactory.getClass().getMethod("getDatabase").invoke(redisConnectionFactory).toString());
		} catch (Exception e) {}

        return op;
    }

    @Override
    public String getPluginName() {
        return RedisPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
