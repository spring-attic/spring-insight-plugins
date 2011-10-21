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

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.collection.method.JoinPointFinalizer;
import com.springsource.insight.collection.method.MethodOperationCollectionAspect;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.util.StringUtil;
import org.aspectj.lang.JoinPoint;
import org.springframework.data.redis.support.collections.AbstractRedisCollection;
import redis.clients.jedis.Jedis;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static com.springsource.insight.util.ListUtil.asSet;
import static java.util.Collections.unmodifiableSet;

/**
 * Driver-level support for redis clients
 */
public aspect RedisClientAspect extends AbstractOperationCollectionAspect {
    public static final OperationType TYPE = OperationType.valueOf("redis-client-method");

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
        Operation op = new Operation().type(TYPE);
        JoinPointFinalizer.register(op, jp);

        String methodName = jp.getSignature().getName();
        Object[] args = jp.getArgs();
        if (args.length >= 1 && args[0] instanceof String) {
            op.label("Redis: " + args[0] + "." + methodName);
        } else {
            op.label("Redis: " + methodName);
        }
        return op;
    }
}
