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

import com.mongodb.*;
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 */
public class MongoCollectionOperationCollectionAspectTest
        extends OperationCollectionAspectTestSupport {

    public void standardAsserts(Operation op) {
        assertEquals(MongoCollectionOperationCollectionAspect.TYPE, op.getType());
        assertEquals("my_super_collection.hello", op.get("collection"));
    }

    private DBCollection getMeACollection() {
        Mongo mongo = mock(Mongo.class);
        DB db = new DBDummy(mongo, "my_super_collection");

        DBCollection col = new DBCollectionDummy(db, "hello");
        return col;
    }

//	execution(WriteResult DBCollection.insert(DBObject[], WriteConcern));
    @Test
    public void testInsert() {
        getMeACollection().insert(new DBObject[0], null);
        Operation op = getLastEntered(Operation.class);
        standardAsserts(op);
    }

//	execution(WriteResult DBCollection.update(DBObject, DBObject, boolean, boolean));
    @Test
    public void testUpdate() {
        getMeACollection().update(null, null, true, true);
        Operation op = getLastEntered(Operation.class);
        standardAsserts(op);
    }

//	execution(WriteResult DBCollection.remove(DBObject, WriteConcern));
    @Test
    public void testRemove() {
        getMeACollection().remove(null, null);
        Operation op = getLastEntered(Operation.class);
        standardAsserts(op);
    }

//	execution(WriteResult DBCollection.save(DBObject, WriteConcern));
    @Test
    @Ignore("This particular method is very difficult to test due to final and various other interactions in the DBCollection class")
    public void testSave() {
        ((DBCollectionDummy)getMeACollection()).save(new BasicDBObject("whoa", "there"), new WriteConcern(), "blah");
        Operation op = getLastEntered(Operation.class);
        standardAsserts(op);
    }

//	execution(DBCursor DBCollection.__find(DBObject, DBObject, int, int, int, int, ReadPreference));
    @Test
    public void testFind() {
        getMeACollection().find(new BasicDBObject("whoa", "there"), new BasicDBObject("whoa", "there"));
        Operation op = getLastEntered(Operation.class);
        standardAsserts(op);
    }
//	execution(void DBCollection.createIndex(DBObject, DBObject));
    @Test
    public void testCreateIndex() {
        getMeACollection().createIndex(new BasicDBObject("hi", "there"));
        Operation op = getLastEntered(Operation.class);
        standardAsserts(op);
    }

//	execution(long DBCollection.getCount(DBObject, DBObject, long, long));
    @Test
    public void testGetCount() {
        getMeACollection().getCount(new BasicDBObject("hi", "there"), new BasicDBObject("blah", "blah"), 1L, 2L);
        Operation op = getLastEntered(Operation.class);
        standardAsserts(op);
    }
//	execution(DBObject DBCollection.group(GroupCommand));
    @Test
    public void testGroup() {
        getMeACollection().group(new BasicDBObject("hi", "there"), new BasicDBObject("blah", "blah"), new BasicDBObject("blah", "blah"), "what do you know");
        Operation op = getLastEntered(Operation.class);
        standardAsserts(op);
    }
//	execution(List DBCollection.distinct(String,DBObject));
    @Test
    public void testDistinct() {
        getMeACollection().distinct("what do you know");
        Operation op = getLastEntered(Operation.class);
        standardAsserts(op);
    }
//	execution(MapReduceOutput DBCollection.mapReduce(..));
    @Test
    public void testMapReduce() {
        getMeACollection().mapReduce(new BasicDBObject("map-reduce", "smoosh"));
        Operation op = getLastEntered(Operation.class);
        standardAsserts(op);
    }
//	execution(void DBCollection.dropIndexes(..));
    @Test
    public void testDropIndexes() {
        getMeACollection().dropIndexes("smoosh");
        Operation op = getLastEntered(Operation.class);
        standardAsserts(op);
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return MongoCollectionOperationCollectionAspect.aspectOf();
    }
}
