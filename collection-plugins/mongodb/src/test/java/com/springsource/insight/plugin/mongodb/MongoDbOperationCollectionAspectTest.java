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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
<<<<<<< HEAD
=======
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
>>>>>>> parent of c2b88cf... Mongo plugin now creates external resources with default metrics

/**
 */
public class MongoDbOperationCollectionAspectTest
        extends OperationCollectionAspectTestSupport {

    // execution(CommandResult DB.command(..));
    @Test
    public void dbCommand() throws Exception {
        Mongo mongo = mock(Mongo.class);
        DB db = new DBDummy(mongo, "my thing");
        db.command("Hello there");
<<<<<<< HEAD
        Operation op = getLastEntered();
=======
        Operation op = (Operation) getLastEntered(Operation.class);
        op.finalizeConstruction();
>>>>>>> parent of c2b88cf... Mongo plugin now creates external resources with default metrics
        assertNotNull(op);
        assertEquals("MongoDB: DB.command()", op.getLabel());
        assertEquals(MongoDbOperationCollectionAspect.TYPE, op.getType());
        assertEquals("Hello there", ((OperationList)op.get("args")).get(0));

    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return MongoDbOperationCollectionAspect.aspectOf();
    }
}
