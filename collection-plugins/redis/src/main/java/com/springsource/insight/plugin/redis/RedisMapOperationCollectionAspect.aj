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
package com.springsource.insight.plugin.redis;

import static com.springsource.insight.plugin.redis.util.RedisUtil.objectToString;

import java.util.Map;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.util.StringUtil;

import org.springframework.data.redis.support.collections.RedisMap;

/**
 * Aspect class for RedisMap operation
 */
public aspect RedisMapOperationCollectionAspect extends AbstractOperationCollectionAspect {

    protected static final OperationType TYPE = OperationType.valueOf("redis-map");

    public pointcut redisMapPut()
        : execution(* RedisMap.put*(..));

    public pointcut redisMapReplace()
        : execution(* RedisMap.replace(..));

    public pointcut redisMapGet()
        : execution(* RedisMap.get(..));

    public pointcut redisMapRemove()
        : execution(* RedisMap.remove(..));

    public pointcut collectionPoint()
        : redisMapPut() || redisMapReplace() || redisMapGet() || redisMapRemove();

    @Override
    protected Operation createOperation(JoinPoint jp) {
        String method = jp.getSignature().getName();
        Operation op = new Operation()
            .type(TYPE)
            .put("method", method);
        RedisMap<?,?> map = (RedisMap<?,?>)jp.getTarget();
        String mapKey = map.getKey();
        if (StringUtil.isEmpty(mapKey)) {
            mapKey = "?";
        }
        op.put("mapKey", mapKey);
        op.label("RedisMap: " + mapKey + "." + method + "()");
        Object[] args = jp.getArgs();
        if(args != null) {
            int argLen = args.length;
            op.put("arglen", argLen);

            if(argLen == 1) {
                if(StringUtil.safeCompare(method, "putAll") == 0) {
                    op.put("size", ((Map<?,?>)args[0]).size());
                }
                else if(StringUtil.safeCompare(method, "get") == 0) {
                    op.put("key", objectToString(args[0]));
                }
                else if(StringUtil.safeCompare(method, "remove") == 0) {
                    op.put("key", objectToString(args[0]));
                }
            }
            else if(argLen == 2) {
                if(StringUtil.safeCompare(method, "put") == 0 ||
                   StringUtil.safeCompare(method, "putIfAbsent") == 0) {
                    op.put("key", objectToString(args[0]));
                    op.put("value", objectToString(args[1]));
                }
                // -- not supported by Spring Redis 1.0.0.M4
                else if(StringUtil.safeCompare(method, "replace") == 0) {
                    op.put("key", objectToString(args[0]));
                    op.put("value", objectToString(args[1]));
                }
                // -- not supported by Spring Redis 1.0.0.M4
                else if(StringUtil.safeCompare(method, "remove") == 0) {
                    op.put("key", objectToString(args[0]));
                    op.put("value", objectToString(args[1]));
                }
            }
            else if(argLen == 3) {
                // -- not supported by Spring Redis 1.0.0.M4
                if(StringUtil.safeCompare(method, "replace") == 0) {
                    op.put("key", objectToString(args[0]));
                    op.put("value", objectToString(args[1]));
                    op.put("newValue", objectToString(args[2]));
                }
            }
        }

        return op;
    }

    @Override
    public String getPluginName() {
        return RedisPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
