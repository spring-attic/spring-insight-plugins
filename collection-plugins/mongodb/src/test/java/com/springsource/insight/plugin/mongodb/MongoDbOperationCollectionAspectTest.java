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

package com.springsource.insight.plugin.mongodb;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 */
public class MongoDbOperationCollectionAspectTest
        extends OperationCollectionAspectTestSupport {

    // execution(CommandResult DB.command(..));
    @Test
    public void dbCommand() throws Exception {
        Mongo mongo = mock(Mongo.class);
        ServerAddress address = mock(ServerAddress.class);
        when(address.getHost()).thenReturn("local");
        when(address.getPort()).thenReturn(27017);
        when(mongo.getAddress()).thenReturn(address);
        DB db = new DBDummy(mongo, "my thing");
        db.command("Hello there");
        Operation op = (Operation) getLastEntered();
        op.finalizeConstruction();
        assertNotNull(op);
        assertEquals("MongoDB: DB.command()", op.getLabel());
        assertEquals(MongoDBOperationAnalyzer.TYPE, op.getType());
        assertEquals("Hello there", ((OperationList)op.get("args")).get(0));

        assertEquals("local", op.get("host", String.class));
        assertEquals("my thing",  op.get("dbName", String.class));
        assertEquals("27017", op.get("port", Integer.class).toString());
    }
    
    @Test
    public void dbCommandNoHost() throws Exception {
        Mongo mongo = mock(Mongo.class);
        DB db = new DBDummy(mongo, "my thing");
        db.command("Hello there");
        Operation op = (Operation) getLastEntered();
        op.finalizeConstruction();
        assertNotNull(op);
        assertEquals("MongoDB: DB.command()", op.getLabel());
        assertEquals(MongoDBOperationAnalyzer.TYPE, op.getType());
        assertEquals("Hello there", ((OperationList)op.get("args")).get(0));

        assertEquals("my thing",  op.get("dbName", String.class));
        assertNull(op.get("host", String.class));
        assertNull(op.get("port", Integer.class));
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return MongoDbOperationCollectionAspect.aspectOf();
    }
}
