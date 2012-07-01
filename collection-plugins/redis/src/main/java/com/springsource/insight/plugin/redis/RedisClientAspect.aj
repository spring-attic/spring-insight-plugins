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

import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.collection.method.JoinPointFinalizer;
import com.springsource.insight.intercept.operation.Operation;

/**
 * Driver-level support for redis clients
 */
public aspect RedisClientAspect extends AbstractOperationCollectionAspect {

    /*
    No JRedis support yet...
    Not readily available in a public maven repository. Everyone uses
    builds manually injected into their local maven repo. The developer
    will apparently release something when the version hits 1.2

    public pointcut jredis()
         : execution(* JRedis.*(..));
    */
    public pointcut jedis()
         : execution(* redis.clients.jedis.Jedis.*(..));

    // Spring-Data uses this for connection management on
    // nearly every call but it isn't useful to us.
    public pointcut jedisQuit()
         : execution(* redis.clients.jedis.Jedis.quit(..));

    public pointcut collectionPoint()
            : jedis() && !jedisQuit() && !cflowbelow(jedis());

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Operation op = new Operation().type(RedisDBAnalyzer.TYPE);
        JoinPointFinalizer.register(op, jp);

        String methodName = jp.getSignature().getName();
        Object[] args = jp.getArgs();
        if (args.length >= 1 && args[0] instanceof String) {
            op.label("Redis: " + args[0] + "." + methodName);
        } else {
            op.label("Redis: " + methodName);
        }
        
        Jedis jedis = (Jedis) jp.getTarget();
        try {
        	Client	client=jedis.getClient();
			op.put("dbName", client.getDB().toString());
			op.put("host", client.getHost());
			op.put("port", client.getPort());
		} catch (Exception e) {
			// ignored
		}
        
        return op;
    }

    @Override
    public String getPluginName() {
        return RedisPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
