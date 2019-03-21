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

package com.springsource.insight.plugin.cassandra;

import com.datastax.driver.core.SimpleStatement;
import com.springsource.insight.intercept.operation.Operation;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;


public class CassandraSimpleStatementOperationCollectionAspectTest {

    @Test
    public void testSimpleStatementCreationNoArgs() {
        SimpleStatement simpleStatement = new SimpleStatement("SELECT * FROM keyspace.table;");
        Operation operation = CassandraOperationFinalizer.get(simpleStatement);
        assertNull(operation);

    }
    @Test
    public void testSimpleStatementCreationWithArgs() {
        SimpleStatement simpleStatement = new SimpleStatement("SELECT * FROM keyspace.table;", "arg1", new Date(), UUID.randomUUID());
        Operation operation = CassandraOperationFinalizer.get(simpleStatement);
        assertNotNull(operation);

    }

}