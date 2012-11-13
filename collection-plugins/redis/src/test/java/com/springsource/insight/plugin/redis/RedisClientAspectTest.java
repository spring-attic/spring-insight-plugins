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

import org.junit.Test;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.util.StringFormatterUtils;

/**
 * Test cases for {@link RedisClientAspect}
 */
public class RedisClientAspectTest extends OperationCollectionAspectTestSupport {
	public RedisClientAspectTest () {
		super();
	}

    @Test
    public void testSet() {
        DummyJedisCommands client = new DummyJedisCommands(null);
        client.set("mykey", "myvalue");
        Operation op = getLastEntered();
        assertNotNull("No operation extracted", op);
        assertEquals("Mismatched method name", "set", op.get("methodName"));
        assertEquals("Mismatched label", "Redis: mykey.set", op.getLabel());
        assertEquals("Mismatched argument value", "mykey", op.get(OperationFields.ARGUMENTS, OperationList.class).get(0));
        assertEquals("Mismatched host", StringFormatterUtils.NULL_VALUE_STRING, op.get("host"));
        assertEquals("Mismatched port", 6379, op.getInt("port", (-1)));
        assertEquals("Mismatched DB name", "0", op.get("dbName"));
    }
    
    @Test
    public void testSetWithDbName() {
        DummyJedisCommands client = new DummyJedisCommands("localhost");
        client.set("mykey", "myvalue");
        Operation op = getLastEntered();
        assertNotNull("No operation extracted", op);
        assertEquals("Mismatched method name", "set", op.get("methodName"));
        assertEquals("Mismatched label", "Redis: mykey.set", op.getLabel());
        assertEquals("Mismatched argument value", "mykey", op.get(OperationFields.ARGUMENTS, OperationList.class).get(0));
        assertEquals("Mismatched host", "localhost", op.get("host"));
        assertEquals("Mismatched port", 6379, op.getInt("port", (-1)));
        assertEquals("Mismatched DB name", "0", op.get("dbName"));
    }

    @Test
    public void testPing() {
        DummyJedisCommands client = new DummyJedisCommands(null);
        client.ping();
        Operation op = getLastEntered();
        assertNotNull("No operation extracted", op);
        assertEquals("Mismatched method name", "ping", op.get("methodName"));
        assertEquals("Mismatched label", "Redis: ping", op.getLabel());
    }

    @Override
    public RedisClientAspect getAspect() {
        return RedisClientAspect.aspectOf();
    }
}
