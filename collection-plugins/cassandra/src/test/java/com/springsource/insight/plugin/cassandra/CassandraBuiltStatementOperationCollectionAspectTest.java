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
package com.springsource.insight.plugin.cassandra;


import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.timestamp;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


public class CassandraBuiltStatementOperationCollectionAspectTest {

    @Test
    public void testBuiltStatementValue() {

        MockSession session = new MockSession();
        Insert insert = QueryBuilder.insertInto("myTable");
        insert.value("arg", "stringvalue");

        Operation operation = CassandraOperationFinalizer.get(insert);
        assertNotNull(operation);

        session.execute(insert);

        assertNotNull(operation.getLabel());
        assertEquals("OperationType", CassandraExternalResourceAnalyzer.TYPE, operation.getType());
        String cql = operation.get("cql", String.class);
        assertEquals("cql", insert.getQueryString(), cql);
        OperationMap params = operation.get(CassandraOperationFinalizer.PARAMS_VALUES, OperationMap.class);
        assertNotNull(params);
        assertEquals("params", params.size(), 1);
        assertNull(CassandraOperationFinalizer.remove(insert));
    }

    @Test
    public void testBuiltStatementValues() {

        MockSession session = new MockSession();
        Insert insert = QueryBuilder.insertInto("myTable");
        insert.values(new String[] {"arg"}, new Object[] {"stringvalue"});

        Operation operation = CassandraOperationFinalizer.get(insert);
        assertNotNull(operation);

        session.execute(insert);

        assertNotNull(operation.getLabel());
        assertEquals("OperationType", CassandraExternalResourceAnalyzer.TYPE, operation.getType());
        String cql = operation.get("cql", String.class);
        assertEquals("cql", insert.getQueryString(), cql);
        OperationMap params = operation.get(CassandraOperationFinalizer.PARAMS_VALUES, OperationMap.class);
        assertNotNull(params);
        assertEquals("params", params.size(), 1);
        assertNull(CassandraOperationFinalizer.remove(insert));
    }

    @Test
    public void testBuiltStatementOptionValue() {

        MockSession session = new MockSession();
        Insert insert = QueryBuilder.insertInto("myTable");
        insert.value("arg", "stringvalue");
        Insert.Options using = insert.using(timestamp(100));
        using.value("another","value");

        Operation operation = CassandraOperationFinalizer.get(insert);
        assertNotNull(operation);

        session.execute(insert);

        assertNotNull(operation.getLabel());
        assertEquals("OperationType", CassandraExternalResourceAnalyzer.TYPE, operation.getType());
        String cql = operation.get("cql", String.class);
        assertEquals("cql", insert.getQueryString(), cql);
        OperationMap params = operation.get(CassandraOperationFinalizer.PARAMS_VALUES, OperationMap.class);
        assertNotNull(params);
        assertEquals("params", params.size(), 2);
        assertNull(CassandraOperationFinalizer.remove(insert));
    }

    @Test
    public void testBuiltStatementOptionValues() {

        MockSession session = new MockSession();
        Insert insert = QueryBuilder.insertInto("myTable");
        insert.values(new String[]{"arg"}, new Object[]{"stringvalue"});

        Insert.Options using = insert.using(timestamp(100));
        using.values(new String[]{"another"}, new Object[]{"value"});

        Operation operation = CassandraOperationFinalizer.get(insert);
        assertNotNull(operation);

        session.execute(insert);

        assertNotNull(operation.getLabel());
        assertEquals("OperationType", CassandraExternalResourceAnalyzer.TYPE, operation.getType());
        String cql = operation.get("cql", String.class);
        assertEquals("cql", insert.getQueryString(), cql);
        OperationMap params = operation.get(CassandraOperationFinalizer.PARAMS_VALUES, OperationMap.class);
        assertNotNull(params);
        assertEquals("params", params.size(), 2);
        assertNull(CassandraOperationFinalizer.remove(insert));
    }

}