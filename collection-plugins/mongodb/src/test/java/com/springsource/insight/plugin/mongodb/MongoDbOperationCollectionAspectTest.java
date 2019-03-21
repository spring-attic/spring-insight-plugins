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

package com.springsource.insight.plugin.mongodb;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.util.StringUtil;

/**
 */
public class MongoDbOperationCollectionAspectTest
        extends OperationCollectionAspectTestSupport {
    public MongoDbOperationCollectionAspectTest() {
        super();
    }

    // execution(CommandResult DB.command(..));
    @SuppressWarnings("boxing")
    @Test
    public void testDbCommandWithHost() throws Exception {
        final String HOST = "7.3.6.5";
        final int PORT = 27017;
        Mongo mongo = mock(Mongo.class);
        ServerAddress address = mock(ServerAddress.class);
        when(address.getHost()).thenReturn(HOST);
        when(address.getPort()).thenReturn(PORT);
        when(mongo.getAddress()).thenReturn(address);

        Operation op = assertCommandOperation(new DBDummy(mongo, "testDbCommandWithHost"));
        assertEquals("Mismatched host", HOST, op.get("host", String.class));
        assertEquals("Mismatched port", 27017, op.getInt("port", (-1)));
    }

    @Test
    public void testDbCommandNoHost() throws Exception {
        Mongo mongo = mock(Mongo.class);
        Operation op = assertCommandOperation(new DBDummy(mongo, "testDbCommandNoHost"));

        for (String key : new String[]{"host", "port"}) {
            assertNullValue("Unexpected value for " + key, op.get(key));
        }
    }

    private Operation assertCommandOperation(DB db) {
        final String argVal = db.getName() + "-arg";
        db.command(argVal);

        Operation op = getLastEntered();
        assertNotNull("No operation extracted", op);
        assertEquals("Mismatched operation type", MongoDBOperationExternalResourceAnalyzer.TYPE, op.getType());
        assertEquals("Mismatched operation label", "MongoDB: DB.command()", op.getLabel());
        assertEquals("Mismatched DB name", db.getName(), op.get("dbName", String.class));

        if (!StringUtil.isEmpty(argVal)) {
            OperationList argsList = op.get("args", OperationList.class);
            assertNotNull("Missing arguments list");

            String actVal = argsList.get(0, String.class);
            assertEquals("Mismatched operation arguments", argVal, actVal);
        }

        return op;
    }

    @Override
    public MongoDbOperationCollectionAspect getAspect() {
        return MongoDbOperationCollectionAspect.aspectOf();
    }
}
