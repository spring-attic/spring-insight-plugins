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
package com.springsource.insight.plugin.jdbc;


import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

@ContextConfiguration("classpath:jdbc-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class JdbcPreparedStatementOperationCollectionAspectTest
    		extends OperationCollectionAspectTestSupport {
    @Autowired DataSource dataSource;

    public JdbcPreparedStatementOperationCollectionAspectTest () {
    	super();
    }

    @Test
    public void operationCollectionForPreparedStatement() throws SQLException {
        Connection c = dataSource.getConnection();
        String sql = "select * from appointment where owner = ? and dateTime = ?";
        PreparedStatement ps = c.prepareStatement(sql);
        ps.setString(1, "Agim");
        ps.setDate(2, Date.valueOf("2009-06-01"));

        ps.execute();

        Operation   operation=getLastEntered();
        assertEquals(sql, operation.get("sql"));
        @SuppressWarnings("unchecked")
        List<String> parameters = (List<String>) operation.asMap().get("params");
        assertEquals(2, parameters.size());
        assertArrayEquals(new Object[] { "Agim", Date.valueOf("2009-06-01").toString() }, parameters.toArray());
    }

    @Test
    public void operationCollectionForCallableStatement() throws SQLException {
        Connection c = dataSource.getConnection();
        String sql = "{call testSp(?)}";

        CallableStatement cs = c.prepareCall(sql);
        
        cs.setString(1, "Agim");
        cs.executeQuery();

        Operation   operation=getLastEntered();
        assertEquals(sql, operation.get("sql"));
        @SuppressWarnings("unchecked")
        List<String> parameters = (List<String>) operation.asMap().get("params");
        assertEquals(1, parameters.size());
        assertArrayEquals(new Object[] { "Agim" }, parameters.toArray());
    }
    
    @Test
    public void operationCollectionForCallableStatementWithNamedParameters() throws SQLException {
        Connection c = dataSource.getConnection();
        String sql = "{call testSp(?)}";

        CallableStatement cs = c.prepareCall(sql);
        
        // So finally, figured out how hsqldb maps named parameters (@p1, @p2 etc.)
        cs.setString("@p1", "someValue");
        cs.executeQuery();

        Operation   operation=getLastEntered();
        assertEquals(sql, operation.get("sql"));
        @SuppressWarnings("unchecked")
        Map<String, String> parameters = (Map<String, String>) operation.asMap().get("params");
        assertEquals(1, parameters.size());
        assertEquals("someValue", parameters.get("@p1"));
    }
    
    @Override
    public OperationCollectionAspectSupport getAspect() {
        return JdbcPreparedStatementOperationCollectionAspect.aspectOf();
    }
}
