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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.data.redis.support.collections.RedisMap;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

/**
 * Test for RedisMap operation collection aspect.
 */
public class RedisMapOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {

    private static final String kw_key = "key";
    private static final String kw_value = "value";
    private static final String kw_size = "size";

    @Test
    public void putAndCollect() {
        String method = "put";
        String key = "key1";
        String value = "value1";

        new DummyRedisMapImpl<String, String>().put(key, value);

        Operation op = standardAsserts(method);
        mapEntrysetAsserts(op, method, key, value);
    }

    @Test
    public void putIfAbsentAndCollect() {
        String method = "putIfAbsent";
        String key = "key2";
        String value = "value2";

        new DummyRedisMapImpl<String, String>().putIfAbsent(key, value);
        standardAsserts(method);
        Operation op = standardAsserts(method);
        mapEntrysetAsserts(op, method, key, value);
    }

    @Test
    public void putAllAndCollect() {
        String method = "putAll";
        String key = "key3";
        String value = "value3";

        Map<String, String> testMap = new HashMap<String, String>();
        testMap.put(key, value);
        new DummyRedisMapImpl<String, String>().putAll(testMap);
        Operation op = standardAsserts(method);
        mapSizeAsserts(op, method, 1);
    }

    @Test
    public void getAndCollect() {
        String method = "get";
        String key = "key2";

        RedisMap<String, String> map = new DummyRedisMapImpl<String, String>();
        map.get(key);

        standardAsserts(method);
        Operation op = standardAsserts(method);
        mapKeyAsserts(op, method, key);
    }

    @Test
    public void replaceAndCollect() {
        String method = "replace";
        String key = "key2";
        String value = "value2";
        String newValue = "newValue";

        RedisMap<String, String> map = new DummyRedisMapImpl<String, String>();
        map.replace(key, value, newValue);

        standardAsserts(method);
        Operation op = standardAsserts(method);
        mapEntrysetAsserts(op, method, key, value);
    }

    @Test
    public void removeAndCollect() {
        String method = "remove";
        String key = "key2";

        RedisMap<String, String> map = new DummyRedisMapImpl<String, String>();
        map.remove(key);

        standardAsserts(method);
        Operation op = standardAsserts(method);
        mapKeyAsserts(op, method, key);
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return RedisMapOperationCollectionAspect.aspectOf();
    }

    private Operation standardAsserts(String method) {
        Operation op = getLastEntered();
        assertEquals("RedisMap: ?." + method + "()", op.getLabel());
        assertEquals(OperationType.valueOf("redis-map"), op.getType());
        return op;
    }

    private Operation mapEntrysetAsserts(Operation op, String method, String key, String value) {
        assertEquals(String.format("Map.%s: key", method, key), key, op.get(kw_key));
        assertEquals(String.format("Map.%s: value of key '%s'", method, key), value, op.get(kw_value));
        return op;
    }

    private Operation mapKeyAsserts(Operation op, String method, String key) {
        assertEquals(String.format("Map.%s: key", method, key), key, op.get(kw_key));
        return op;
    }

    private Operation mapSizeAsserts(Operation op, String method, int size) {
        assertEquals(String.format("Map.%s: size", method), new Integer(size), op.get(kw_size));
        return op;
    }
}
