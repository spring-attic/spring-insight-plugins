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
package com.springsource.insight.plugin.jdbc;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp.DataSourceConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

@ContextConfiguration("classpath:jdbc-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class PoolingConnectionTest
    	extends OperationCollectionAspectTestSupport {
    @Autowired
    DataSource dataSource;

    public PoolingConnectionTest () {
    	super();
    }

    @Test
    public void operationCollection() throws SQLException {
        DataSourceConnectionFactory connFactory = new DataSourceConnectionFactory(dataSource);
        ObjectPool connPool = new GenericObjectPool();
        PoolableConnectionFactory poolFactory = new PoolableConnectionFactory(connFactory, connPool, null, null, false, true);
        PoolingDataSource poolDs = new PoolingDataSource(poolFactory.getPool());
        
        String sql = "select * from appointment where owner = 'Agim'";
        Connection c = poolDs.getConnection();
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            try {
                System.out.println("Prepared statement=" + ps.getClass());
        
                ResultSet   rs=ps.executeQuery();
                rs.close();
            } finally {
                ps.close();
            }
        } finally {
            c.close();
        }
        
        ArgumentCaptor<Operation> opCaptor = ArgumentCaptor.forClass(Operation.class);
        verify(spiedOperationCollector, times(3)).enter(opCaptor.capture());

        List<Operation> ops = opCaptor.getAllValues();
        assertEquals(3, ops.size());
        assertTrue(ops.get(0).getSourceCodeLocation().toString().startsWith("org.apache.commons.dbcp.PoolingDataSource$PoolGuardConnectionWrapper#prepareStatement:"));
        assertTrue(ops.get(1).getSourceCodeLocation().toString().startsWith("org.apache.commons.dbcp.DelegatingConnection#prepareStatement:"));
        assertTrue(ops.get(2).getSourceCodeLocation().toString().startsWith("org.hsqldb.jdbc.jdbcConnection#prepareStatement:"));
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return JdbcPreparedStatementOperationCollectionAspect.aspectOf();
    }

}
