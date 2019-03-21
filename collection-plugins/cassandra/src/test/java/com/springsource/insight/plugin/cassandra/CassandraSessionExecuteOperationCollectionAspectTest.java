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


import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class CassandraSessionExecuteOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {


    @Test
    public void testExecuteQuery() {
        Session session = new MockSession();
        String cqlQuery = "SELECT * FROM keyspace.table";
        session.execute(cqlQuery);
        assertEquals(0,CassandraOperationFinalizer.storage.size());
        Operation operation = getLastEntered();
        assertNotNull(operation);
        assertNotNull(operation.getLabel());
        assertEquals("OperationType", CassandraExternalResourceAnalyzer.TYPE, operation.getType());
        String cql = operation.get("cql", String.class);
        assertEquals("cql", cqlQuery, cql);
        assertEquals("keyspace", session.getLoggedKeyspace(), operation.get(CassandraOperationFinalizer.KEYSPACE,String.class));
        assertEquals("clustername", session.getCluster().getMetadata().getClusterName(), operation.get(CassandraOperationFinalizer.CLUSTER_NAME,String.class));
        assertEquals("port", session.getCluster().getConfiguration().getProtocolOptions().getPort(), operation.getInt(CassandraOperationFinalizer.PORT, -1));

    }

    @Test
    public void testExecuteQueryWithArgs() {

        Session session = new MockSession();
        String cqlQuery = "SELECT * FROM keyspace.table";
        session.execute(cqlQuery,"arg1", new Date(), UUID.randomUUID());
        assertEquals(0,CassandraOperationFinalizer.storage.size());
        Operation operation = getLastEntered();
        assertNotNull(operation);
        assertNotNull(operation.getLabel());
        assertEquals("OperationType", CassandraExternalResourceAnalyzer.TYPE, operation.getType());
        String cql = operation.get("cql", String.class);
        assertEquals("cql", cqlQuery, cql);
        assertEquals("keyspace", session.getLoggedKeyspace(), operation.get(CassandraOperationFinalizer.KEYSPACE, String.class));
        assertEquals("clustername", session.getCluster().getMetadata().getClusterName(), operation.get(CassandraOperationFinalizer.CLUSTER_NAME, String.class));
        assertEquals("port", session.getCluster().getConfiguration().getProtocolOptions().getPort(), operation.getInt(CassandraOperationFinalizer.PORT, -1));

        OperationMap params = operation.get(CassandraOperationFinalizer.PARAMS_VALUES, OperationMap.class);
        assertNotNull(params);
        assertEquals("params", params.size(), 3);

    }

    @Test
    public void testExecuteBoundStatement() {

        Session session = new MockSession();
        PreparedStatement preparedStatement = new MockPreparedStatement();
        BoundStatement boundStatement = preparedStatement.bind();
        session.execute(boundStatement);
        assertEquals(0,CassandraOperationFinalizer.storage.size());
        Operation operation = getLastEntered();
        assertNotNull(operation);
        assertNotNull(operation.getLabel());
        assertEquals("OperationType", CassandraExternalResourceAnalyzer.TYPE, operation.getType());
        String cql = operation.get("cql", String.class);
        assertEquals("cql", preparedStatement.getQueryString(), cql);

        assertEquals("keyspace", boundStatement.getKeyspace(), operation.get(CassandraOperationFinalizer.KEYSPACE, String.class));
        assertEquals("clustername", session.getCluster().getMetadata().getClusterName(), operation.get(CassandraOperationFinalizer.CLUSTER_NAME, String.class));
        assertEquals("port", session.getCluster().getConfiguration().getProtocolOptions().getPort(), operation.getInt(CassandraOperationFinalizer.PORT, -1));


    }

    @Test
    public void testExecuteSimpleStatement() {

        Session session = new MockSession();
        String queryString = "SELECT * FROM keyspace.table;";
        SimpleStatement simple = new SimpleStatement(queryString);
        simple.setKeyspace("LoggedKeyspace");
        session.execute(simple);
        assertEquals(0,CassandraOperationFinalizer.storage.size());
        Operation operation = getLastEntered();
        assertNotNull(operation);
        assertNotNull(operation.getLabel());
        assertEquals("OperationType", CassandraExternalResourceAnalyzer.TYPE, operation.getType());
        String cql = operation.get("cql", String.class);
        assertEquals("cql", queryString, cql);

        assertEquals("keyspace", simple.getKeyspace(), operation.get(CassandraOperationFinalizer.KEYSPACE, String.class));
        assertEquals("clustername", session.getCluster().getMetadata().getClusterName(), operation.get(CassandraOperationFinalizer.CLUSTER_NAME, String.class));
        assertEquals("port", session.getCluster().getConfiguration().getProtocolOptions().getPort(), operation.getInt(CassandraOperationFinalizer.PORT, -1));

    }

    @Test
    public void testExecuteBatchStatement() {

        Session session = new MockSession();
        String queryString1 = "SELECT * FROM keyspace1.table1;";
        SimpleStatement simple1 = new SimpleStatement(queryString1);
        String queryString2 = "SELECT * FROM keyspace1.table2;";
        SimpleStatement simple2 = new SimpleStatement(queryString2);
        BatchStatement batch = new BatchStatement();
        batch.add(simple1);
        batch.add(simple2);
        session.execute(batch);
        assertEquals(0,CassandraOperationFinalizer.storage.size());
        Operation operation = getLastEntered();
        assertNotNull(operation);
        assertNotNull(operation.getLabel());
        assertEquals("OperationType", CassandraExternalResourceAnalyzer.TYPE, operation.getType());
        String cql = operation.get("cql", String.class);
        assertEquals("cql", CassandraOperationFinalizer.UNKNOWN_CQL + " BATCH ", cql);

        assertEquals("keyspace", session.getLoggedKeyspace(), operation.get(CassandraOperationFinalizer.KEYSPACE, String.class));
        assertEquals("clustername", session.getCluster().getMetadata().getClusterName(), operation.get(CassandraOperationFinalizer.CLUSTER_NAME, String.class));
        assertEquals("port", session.getCluster().getConfiguration().getProtocolOptions().getPort(), operation.getInt(CassandraOperationFinalizer.PORT, -1));

    }

    @Test
    public void testExecuteRegularStatement() {

        Session session = new MockSession();
        TestRegularStatement regularStatement = new TestRegularStatement();
        session.execute(regularStatement);
        assertEquals(0,CassandraOperationFinalizer.storage.size());
        Operation operation = getLastEntered();
        assertNotNull(operation);
        assertNotNull(operation.getLabel());
        assertEquals("OperationType", CassandraExternalResourceAnalyzer.TYPE, operation.getType());
        String cql = operation.get("cql", String.class);
        assertEquals("cql", regularStatement.getQueryString(), cql);

        assertEquals("keyspace", regularStatement.getKeyspace(), operation.get(CassandraOperationFinalizer.KEYSPACE, String.class));
        assertEquals("clustername", session.getCluster().getMetadata().getClusterName(), operation.get(CassandraOperationFinalizer.CLUSTER_NAME, String.class));
        assertEquals("port", session.getCluster().getConfiguration().getProtocolOptions().getPort(), operation.getInt(CassandraOperationFinalizer.PORT, -1));
    }

    @Test
    public void testExecuteBuiltStatement() {

        Session session = new MockSession();
        Insert insert = QueryBuilder.insertInto("keyspace", "table")
                .ifNotExists().value("foo", "bar");
        session.execute(insert);
        assertEquals(0,CassandraOperationFinalizer.storage.size());
        Operation operation = getLastEntered();
        assertNotNull(operation);
        assertNotNull(operation.getLabel());
        assertEquals("OperationType", CassandraExternalResourceAnalyzer.TYPE, operation.getType());
        String cql = operation.get("cql", String.class);
        assertEquals("cql", insert.getQueryString(), cql);

        assertEquals("keyspace", insert.getKeyspace(), operation.get(CassandraOperationFinalizer.KEYSPACE, String.class));
        assertEquals("clustername", session.getCluster().getMetadata().getClusterName(), operation.get(CassandraOperationFinalizer.CLUSTER_NAME, String.class));
        assertEquals("port", session.getCluster().getConfiguration().getProtocolOptions().getPort(), operation.getInt(CassandraOperationFinalizer.PORT, -1));

    }

    @Test
    public void testExecuteAsyncQuery() {
        Session session = new MockSession();
        String cqlQuery = "SELECT * FROM keyspace.table";
        session.executeAsync(cqlQuery);
        assertEquals(0,CassandraOperationFinalizer.storage.size());
        Operation operation = getLastEntered();
        assertNotNull(operation);
        assertNotNull(operation.getLabel());
        assertEquals("OperationType", CassandraExternalResourceAnalyzer.TYPE, operation.getType());
        String cql = operation.get("cql", String.class);
        assertEquals("cql", cqlQuery, cql);
        assertEquals("keyspace", session.getLoggedKeyspace(), operation.get(CassandraOperationFinalizer.KEYSPACE,String.class));
        assertEquals("clustername", session.getCluster().getMetadata().getClusterName(), operation.get(CassandraOperationFinalizer.CLUSTER_NAME,String.class));
        assertEquals("port", session.getCluster().getConfiguration().getProtocolOptions().getPort(), operation.getInt(CassandraOperationFinalizer.PORT, -1));

    }
    @Test
    public void testExecuteAsyncQueryWithArgs() {

        Session session = new MockSession();
        String cqlQuery = "SELECT * FROM keyspace.table";
        session.executeAsync(cqlQuery, "arg1", new Date(), UUID.randomUUID());
        assertEquals(0,CassandraOperationFinalizer.storage.size());
        Operation operation = getLastEntered();
        assertNotNull(operation);
        assertNotNull(operation.getLabel());
        assertEquals("OperationType", CassandraExternalResourceAnalyzer.TYPE, operation.getType());
        String cql = operation.get("cql", String.class);
        assertEquals("cql", cqlQuery, cql);
        assertEquals("keyspace", session.getLoggedKeyspace(), operation.get(CassandraOperationFinalizer.KEYSPACE, String.class));
        assertEquals("clustername", session.getCluster().getMetadata().getClusterName(), operation.get(CassandraOperationFinalizer.CLUSTER_NAME, String.class));
        assertEquals("port", session.getCluster().getConfiguration().getProtocolOptions().getPort(), operation.getInt(CassandraOperationFinalizer.PORT, -1));

        OperationMap params = operation.get(CassandraOperationFinalizer.PARAMS_VALUES, OperationMap.class);
        assertNotNull(params);
        assertEquals("params", params.size(), 3);

    }
    @Test
    public void testExecuteAsyncBoundStatement() {

        Session session = new MockSession();
        PreparedStatement preparedStatement = new MockPreparedStatement();
        BoundStatement boundStatement = preparedStatement.bind();
        session.executeAsync(boundStatement);
        assertEquals(0,CassandraOperationFinalizer.storage.size());
        Operation operation = getLastEntered();
        assertNotNull(operation);
        assertNotNull(operation.getLabel());
        assertEquals("OperationType", CassandraExternalResourceAnalyzer.TYPE, operation.getType());
        String cql = operation.get("cql", String.class);
        assertEquals("cql", preparedStatement.getQueryString(), cql);

        assertEquals("keyspace", boundStatement.getKeyspace(), operation.get(CassandraOperationFinalizer.KEYSPACE, String.class));
        assertEquals("clustername", session.getCluster().getMetadata().getClusterName(), operation.get(CassandraOperationFinalizer.CLUSTER_NAME, String.class));
        assertEquals("port", session.getCluster().getConfiguration().getProtocolOptions().getPort(), operation.getInt(CassandraOperationFinalizer.PORT, -1));

    }

    @Test
    public void testExecuteAsyncBuiltStatement() {

        Session session = new MockSession();
        Insert insert = QueryBuilder.insertInto("keyspace", "table")
                .ifNotExists().value("foo", "bar");
        session.executeAsync(insert);
        assertEquals(0,CassandraOperationFinalizer.storage.size());
        Operation operation = getLastEntered();
        assertNotNull(operation);
        assertNotNull(operation.getLabel());
        assertEquals("OperationType", CassandraExternalResourceAnalyzer.TYPE, operation.getType());
        String cql = operation.get("cql", String.class);
        assertEquals("cql", insert.getQueryString(), cql);

        assertEquals("keyspace", insert.getKeyspace(), operation.get(CassandraOperationFinalizer.KEYSPACE, String.class));
        assertEquals("clustername", session.getCluster().getMetadata().getClusterName(), operation.get(CassandraOperationFinalizer.CLUSTER_NAME, String.class));
        assertEquals("port", session.getCluster().getConfiguration().getProtocolOptions().getPort(), operation.getInt(CassandraOperationFinalizer.PORT, -1));

    }


    @Override
    public OperationCollectionAspectSupport getAspect() {
        return CassandraSessionExecuteOperationCollectionAspect.aspectOf();
    }

    private static class TestRegularStatement extends RegularStatement {

        @Override
        public String getQueryString() {
            return "SELECT * from keyspace.table;";
        }

        @Override
        public ByteBuffer[] getValues() {
            return new ByteBuffer[0];
        }

        @Override
        public ByteBuffer getRoutingKey() {
            return null;
        }

        @Override
        public String getKeyspace() {
            return "Keyspace";
        }
    }

}

