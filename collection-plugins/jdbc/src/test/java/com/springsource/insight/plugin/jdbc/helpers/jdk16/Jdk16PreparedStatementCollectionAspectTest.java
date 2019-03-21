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
package com.springsource.insight.plugin.jdbc.helpers.jdk16;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.plugin.jdbc.JdbcOperationFinalizer;
import com.springsource.insight.plugin.jdbc.JdbcPreparedStatementOperationCollectionAspect;
import com.springsource.insight.plugin.jdbc.JdbcStatementOperationCollectionTestSupport;
import com.springsource.insight.plugin.jdbc.helpers.AbstractConnection;
import com.springsource.insight.plugin.jdbc.helpers.AbstractStatement;
import com.springsource.insight.util.ObjectUtil;

/**
 *
 */
public class Jdk16PreparedStatementCollectionAspectTest extends JdbcStatementOperationCollectionTestSupport {
    private final AtomicInteger callsCount = new AtomicInteger(0);
    private final AbstractStatement metaDataStmt = new Jdk16Statement();
    private final DatabaseMetaData metaData = new Jdk16DatabaseMetaData() {
        @SuppressWarnings("synthetic-access")
        @Override
        public String getURL() throws SQLException {
            callsCount.incrementAndGet();
            Connection c = metaDataStmt.getConnection();
            try {
                CallableStatement cs = c.prepareCall("SHOW META DATA");
                try {
                    cs.executeQuery();
                } finally {
                    cs.close();
                }
            } finally {
                c.close();
            }
            return "jdbc:test:call=" + callsCount;
        }
    };
    private final AbstractConnection metaDataConn = new Jdk16Connection() {
        @SuppressWarnings("synthetic-access")
        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            return metaData;
        }
    };
    private final AbstractStatement testStmt = new Jdk16Statement();

    public Jdk16PreparedStatementCollectionAspectTest() {
        metaDataStmt.setConnection(metaDataConn);
        metaDataConn.setStatement(testStmt);
        testStmt.setConnection(metaDataConn);
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
        callsCount.set(0);
    }

    @Test
    public void testPreparedGetMetadataRunsSqlQuery() throws SQLException {
        final String sql = "select * from appointment where owner = 'Agim' and dateTime = '2009-06-01'";
        PreparedStatement ps = metaDataConn.prepareStatement(sql);
        try {
            assertTrue("Failed to execute", ps.execute());
        } finally {
            ps.close();
        }

        assertEquals("Mismatched meta data calls count", 1, callsCount.intValue());
        assertJdbcOperation(sql);
    }

    @Test
    public void testCallableGetMetadataRunsSqlQuery() throws SQLException {
        final String sql = "callMe()";
        CallableStatement ps = metaDataConn.prepareCall(sql);
        try {
            ps.executeQuery();
        } finally {
            ps.close();
        }

        assertEquals("Mismatched meta data calls count", 1, callsCount.intValue());
        assertJdbcOperation(sql);
    }

    @Test
    public void testReusePreparedIndexedParameterStatement() throws SQLException {
        final String sql = "select * from tests where test = 'testReusePreparedIndexedParameterStatement' and index < ?";
        PreparedStatement ps = metaDataConn.prepareStatement(sql);
        final int NUM_ITERATIONS = Byte.SIZE;
        for (int index = 0; index < NUM_ITERATIONS; index++) {
            ps.setInt(1, index);
            assertTrue("Failed to execute for index=" + index, ps.execute());
        }

        ArgumentCaptor<Operation> opCaptor = ArgumentCaptor.forClass(Operation.class);
        verify(spiedOperationCollector, times(NUM_ITERATIONS)).enter(opCaptor.capture());

        List<Operation> ops = opCaptor.getAllValues();
        assertEquals("Mismatched number of operations", NUM_ITERATIONS, ops.size());

        Set<Operation> opsList = new TreeSet<Operation>(ObjectUtil.OBJECT_INSTANCE_COMPARATOR);
        for (int index = 0; index < ops.size(); index++) {
            Operation op = assertJdbcOperation(ops.get(index), sql);
            assertFalse("Non-unique operation for index=" + index, opsList.contains(op));

            OperationList params = op.get(JdbcOperationFinalizer.PARAMS_VALUES, OperationList.class);
            assertNotNull("No parameters for index=" + index, params);
            assertEquals("Mismatched #params for index=" + index, 1, params.size());
            assertEquals("Mismatched value for index=" + index, String.valueOf(index), params.get(0));
        }
    }

    @Test
    public void testReusePreparedMappedParameterStatement() throws SQLException {
        final String sql = "select * from tests where test = 'testReusePreparedMappedParameterStatement' and index < ?";
        CallableStatement ps = metaDataConn.prepareCall(sql);
        final int NUM_ITERATIONS = Byte.SIZE;
        final String TEST_PARAM = "testIndex";
        for (int index = 0; index < NUM_ITERATIONS; index++) {
            ps.setInt(TEST_PARAM, index);
            assertTrue("Failed to execute for index=" + index, ps.execute());
        }

        ArgumentCaptor<Operation> opCaptor = ArgumentCaptor.forClass(Operation.class);
        verify(spiedOperationCollector, times(NUM_ITERATIONS)).enter(opCaptor.capture());

        List<Operation> ops = opCaptor.getAllValues();
        assertEquals("Mismatched number of operations", NUM_ITERATIONS, ops.size());

        Set<Operation> opsList = new TreeSet<Operation>(ObjectUtil.OBJECT_INSTANCE_COMPARATOR);
        for (int index = 0; index < ops.size(); index++) {
            Operation op = assertJdbcOperation(ops.get(index), sql);
            assertFalse("Non-unique operation for index=" + index, opsList.contains(op));

            OperationMap params = op.get(JdbcOperationFinalizer.PARAMS_VALUES, OperationMap.class);
            assertNotNull("No parameters for index=" + index, params);
            assertEquals("Mismatched #params for index=" + index, 1, params.size());
            assertEquals("Mismatched value for index=" + index, String.valueOf(index), params.get(TEST_PARAM));
        }
    }

    @Override
    public JdbcPreparedStatementOperationCollectionAspect getAspect() {
        return JdbcPreparedStatementOperationCollectionAspect.aspectOf();
    }
}
