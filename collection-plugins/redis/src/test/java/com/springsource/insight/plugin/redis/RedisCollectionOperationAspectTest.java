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

package com.springsource.insight.plugin.redis;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.data.redis.support.collections.AbstractRedisCollection;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

/**
 * Test cases for RedisCollectionOperationAspect
 */
public class RedisCollectionOperationAspectTest extends OperationCollectionAspectTestSupport {

    private static final String kw_value = "value";
    private static final String kw_size = "size";

    public RedisCollectionOperationAspectTest() {
        super();
    }

    @Test
    public void addAndCollect() {
        String method = "add";
        String value = "value2";

        AbstractRedisCollection<String> collection = new DummyAbstractRedisCollection<String>();
        collection.add(value);

        standardAsserts(method);
        Operation op = standardAsserts(method);
        collectionAsserts(op, method, value);
    }

    @Test
    public void addAllAndCollect() {
        String method = "addAll";
        String value = "value3";

        AbstractRedisCollection<String> collection = new DummyAbstractRedisCollection<String>();
        List<String> list = new ArrayList<String>();
        list.add(value);
        collection.addAll(list);

        standardAsserts(method);
        Operation op = standardAsserts(method);
        collectionSizeAsserts(op, method, 1);
    }

    @Test
    public void removeAndCollect() {
        String method = "remove";
        String value = "value2";

        AbstractRedisCollection<String> collection = new DummyAbstractRedisCollection<String>();
        collection.remove(value);

        standardAsserts(method);
        Operation op = standardAsserts(method);
        collectionAsserts(op, method, value);
    }

    @Test
    public void removeAllAndCollect() {
        String method = "removeAll";
        String value = "value3";

        AbstractRedisCollection<String> collection = new DummyAbstractRedisCollection<String>();
        List<String> list = new ArrayList<String>();
        list.add(value);
        collection.removeAll(list);

        standardAsserts(method);
        Operation op = standardAsserts(method);
        collectionSizeAsserts(op, method, 1);
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return RedisCollectionOperationAspect.aspectOf();
    }

    private Operation standardAsserts(String method) {
        Operation op = getLastEntered();
        assertEquals("RedisCollection: ?." + method + "()", op.getLabel());
        assertEquals(OperationType.valueOf("default-redis-collection"), op.getType());
        return op;
    }

    private Operation collectionAsserts(Operation op, String method, String value) {
        assertEquals(String.format("RedisCollection.%s: value", method, value), value, op.get(kw_value));
        return op;
    }

    private Operation collectionSizeAsserts(Operation op, String method, int size) {
        assertEquals(String.format("RedisCollection.%s: size", method), size, op.getInt(kw_size, (-1)));
        return op;
    }

}
