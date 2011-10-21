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

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import java.util.List;

/**
 */
public class DBCursorDummy extends DBCursor {
    public DBCursorDummy(DBCollection collection, DBObject q, DBObject k) {
        super(collection, q, k);
    }

    @Override
    public DBObject next() {
        return null;
    }

    @Override
    public DBCursor skip(int num) {
        return null;
    }

    @Override
    public DBCursor limit(int n) {
        return null;
    }

    @Override
    public DBCursor batchSize(int n) {
        return null;
    }

    @Override
    public List<DBObject> toArray() {
        return null;
    }

    @Override
    public List<DBObject> toArray(int n) {
        return null;
    }

    @Override
    public DBCursor sort(DBObject obj) {
        return null;
    }

}
