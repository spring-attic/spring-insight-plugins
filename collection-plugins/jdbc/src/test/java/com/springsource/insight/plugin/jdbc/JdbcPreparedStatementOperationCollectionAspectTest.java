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

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.ListUtil;

@ContextConfiguration("classpath:jdbc-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class JdbcPreparedStatementOperationCollectionAspectTest extends JdbcStatementOperationCollectionTestSupport {
    @Autowired private DataSource dataSource;

    public JdbcPreparedStatementOperationCollectionAspectTest () {
    	super();
    }

    @Test
    public void testOperationCollectionForPreparedStatement() throws SQLException {
    	final String	sql = "select * from appointment where owner = ? and dateTime = ?";
    	final String	strParam="Agim";
    	final Date		dtParam=Date.valueOf("2009-06-01");
        Connection	 	c = dataSource.getConnection();
        try {
        	PreparedStatement ps = c.prepareStatement(sql);
        	try {
	        	ps.setString(1, strParam);
	        	ps.setDate(2, dtParam);

	        	ps.execute();
        	} finally {
        		ps.close();
        	}
        } finally {
        	c.close();
        }

        Operation   operation=assertJdbcOperation(sql);
        assertSqlParams(operation, strParam, dtParam.toString());
    }

    @Test
    public void testOperationCollectionForCallableStatement() throws SQLException {
        final String	sql = "{call testSp(?)}";
        final String	strParam = "Agim";
        Connection c = dataSource.getConnection();
        try {
        	CallableStatement cs = c.prepareCall(sql);
        	try {
        		cs.setString(1, strParam);
        		cs.executeQuery();
        	} finally {
        		cs.close();
        	}
        } finally {
        	c.close();
        }

        Operation   operation=assertJdbcOperation(sql);
        assertSqlParams(operation, strParam);
    }
    
    @Test
    public void testOperationCollectionForCallableStatementWithNamedParameters() throws SQLException {
        final String sql = "{call testSp(?)}", paramName = "@p1", paramValue="someValue";
        Connection c = dataSource.getConnection();
        try {
	        CallableStatement cs = c.prepareCall(sql);
	        
	        // So finally, figured out how hsqldb maps named parameters (@p1, @p2 etc.)
	        try {
	        	cs.setString(paramName, paramValue);
	        	cs.executeQuery();
	        } finally {
	        	cs.close();
	        }
        } finally {
        	c.close();
        }

        Operation   operation=assertJdbcOperation(sql);

        @SuppressWarnings("unchecked")
        Map<String, String> parameters = (Map<String, String>) operation.asMap().get("params");
        assertEquals("Mismatched parameters count", 1, parameters.size());
        assertEquals("Mismatched parameter value", paramValue, parameters.get(paramName));
    }

    protected List<String> assertSqlParams (Operation	op, String ... values) {
        @SuppressWarnings("unchecked")
        List<String> parameters = (List<String>) op.asMap().get("params");
        assertEquals("Mismatched parameters count", ArrayUtil.length(values), ListUtil.size(parameters));
        assertArrayEquals("Mismatched parameters values", values, parameters.toArray(new String[parameters.size()]));
        return parameters;
    }

    @Override
    public JdbcPreparedStatementOperationCollectionAspect getAspect() {
        return JdbcPreparedStatementOperationCollectionAspect.aspectOf();
    }
}
