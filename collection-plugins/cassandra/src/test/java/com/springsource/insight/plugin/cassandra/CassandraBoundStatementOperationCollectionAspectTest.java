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


import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ColumnDefinitions;
import org.junit.Before;
import org.junit.Test;

import com.datastax.driver.core.PreparedStatement;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.StringUtil;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CassandraBoundStatementOperationCollectionAspectTest {

    public CassandraBoundStatementOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testBoundStatementCreationParameters() {
        PreparedStatement preparedStatement = new MockPreparedStatement();
        BoundStatement bind = preparedStatement.bind("arg1", new Date(), UUID.randomUUID());
        Operation operation = CassandraOperationFinalizer.get(bind);
        assertNotNull(operation);
        assertNotNull(operation.getLabel());
        assertEquals("OperationType", CassandraExternalResourceAnalyzer.TYPE, operation.getType());
        String cql = operation.get("cql", String.class);
        assertEquals("cql", preparedStatement.getQueryString(), cql);
        OperationMap params = operation.get(CassandraOperationFinalizer.PARAMS_VALUES, OperationMap.class);
        assertNotNull(params);
        assertEquals("params", params.size(), 3);
        CassandraOperationFinalizer.remove(bind);
    }

    @Test
    public void testBoundStatementBindNoParameters() {

        String queryString = "SELECT * from keyspace.table";
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.getQueryString()).thenReturn(queryString);
        ColumnDefinitions columnDefinitions = mock(ColumnDefinitions.class);
        when(columnDefinitions.size()).thenReturn(0);
        when(preparedStatement.getVariables()).thenReturn(columnDefinitions);
        BoundStatement boundStatement = new MockBoundStatement(preparedStatement);
        BoundStatement bind = boundStatement.bind();
        Operation operation = CassandraOperationFinalizer.get(bind);
        assertTrue(bind == boundStatement);
        assertNotNull(operation);
        assertNotNull(operation.getLabel());
        assertEquals("OperationType", CassandraExternalResourceAnalyzer.TYPE, operation.getType());
        String cql = operation.get("cql", String.class);
        assertEquals("cql", preparedStatement.getQueryString(), cql);
        CassandraOperationFinalizer.remove(bind);
    }

    @Test
    public void testBoundStatementBindParameters() {

        String queryString = "SELECT * from keyspace.table";
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.getQueryString()).thenReturn(queryString);
        ColumnDefinitions columnDefinitions = mock(ColumnDefinitions.class);
        when(columnDefinitions.size()).thenReturn(0);
        when(preparedStatement.getVariables()).thenReturn(columnDefinitions);
        BoundStatement boundStatement = new MockBoundStatement(preparedStatement);
        BoundStatement bind = boundStatement.bind("arg1", new Date(), UUID.randomUUID());
        Operation operation = CassandraOperationFinalizer.get(bind);
        assertTrue(bind == boundStatement);
        assertNotNull(operation);
        assertNotNull(operation.getLabel());
        assertEquals("OperationType", CassandraExternalResourceAnalyzer.TYPE, operation.getType());
        String cql = operation.get("cql", String.class);
        assertEquals("cql", preparedStatement.getQueryString(), cql);
        OperationMap params = operation.get(CassandraOperationFinalizer.PARAMS_VALUES, OperationMap.class);
        assertNotNull(params);
        assertEquals("params", params.size(), 3);
        CassandraOperationFinalizer.remove(bind);
    }

    @Test
    public void testBoundStatementSetByIndexParameter() {

        String queryString = "SELECT * from keyspace.table";
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.getQueryString()).thenReturn(queryString);
        ColumnDefinitions columnDefinitions = mock(ColumnDefinitions.class);
        when(columnDefinitions.size()).thenReturn(0);
        when(preparedStatement.getVariables()).thenReturn(columnDefinitions);
        BoundStatement boundStatement = new MockBoundStatement(preparedStatement);
        boundStatement.setBool(1,true);
        boundStatement.setDate(2, new Date());
        boundStatement.setDouble(3, 5.0d);
        BoundStatement bind = boundStatement.bind();
        Operation operation = CassandraOperationFinalizer.get(bind);
        assertTrue(bind == boundStatement);
        assertNotNull(operation);
        assertNotNull(operation.getLabel());
        assertEquals("OperationType", CassandraExternalResourceAnalyzer.TYPE, operation.getType());
        String cql = operation.get("cql", String.class);
        assertEquals("cql", preparedStatement.getQueryString(), cql);

        OperationMap params = operation.get(CassandraOperationFinalizer.PARAMS_VALUES, OperationMap.class);
        assertNotNull(params);
        assertEquals("params", params.size(), 3);
        CassandraOperationFinalizer.remove(bind);
    }
    @Test
    public void testBoundStatementSetByNameParameter() {

        String queryString = "SELECT * from keyspace.table";
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.getQueryString()).thenReturn(queryString);
        ColumnDefinitions columnDefinitions = mock(ColumnDefinitions.class);
        when(columnDefinitions.size()).thenReturn(0);
        when(preparedStatement.getVariables()).thenReturn(columnDefinitions);
        BoundStatement boundStatement = new MockBoundStatement(preparedStatement);
        boundStatement.setBool("bool",true);
        boundStatement.setDate("date", new Date());
        boundStatement.setDouble("double", 5.0d);
        BoundStatement bind = boundStatement.bind();
        Operation operation = CassandraOperationFinalizer.get(bind);
        assertTrue(bind == boundStatement);
        assertNotNull(operation);
        assertNotNull(operation.getLabel());
        assertEquals("OperationType", CassandraExternalResourceAnalyzer.TYPE, operation.getType());
        String cql = operation.get("cql", String.class);
        assertEquals("cql", preparedStatement.getQueryString(), cql);

        OperationMap params = operation.get(CassandraOperationFinalizer.PARAMS_VALUES, OperationMap.class);
        assertNotNull(params);
        assertEquals("params", params.size(), 3);
        CassandraOperationFinalizer.remove(bind);
    }


}
