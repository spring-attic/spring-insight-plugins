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
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.StringUtil;

@ContextConfiguration("classpath:jdbc-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class JdbcPreparedStatementOperationCollectionAspectTest extends JdbcStatementOperationCollectionTestSupport {
    @Autowired
    private DataSource dataSource;

    public JdbcPreparedStatementOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testOperationCollectionForPreparedStatement() throws SQLException {
        final String sql = "select * from appointment where owner = ? and dateTime = ?";
        final String strParam = "Agim";
        final Date dtParam = Date.valueOf("2009-06-01");
        Connection c = dataSource.getConnection();
        try {
            PreparedStatement ps = c.prepareStatement(sql);
            try {
                ps.setString(1, strParam);
                ps.setDate(2, dtParam);

                assertTrue("Failed to execute", ps.execute());
            } finally {
                ps.close();
            }
        } finally {
            c.close();
        }

        Operation operation = assertJdbcOperation(sql);
        assertSqlParams(operation, strParam, dtParam.toString());
    }

    @Test
    public void testOperationCollectionForCallableStatement() throws SQLException {
        final String sql = "{call testSp(?)}";
        final String strParam = "Agim";
        Connection c = dataSource.getConnection();
        try {
            CallableStatement cs = c.prepareCall(sql);
            try {
                cs.setString(1, strParam);

                ResultSet returnValue = cs.executeQuery();
                if (returnValue != null) {
                    returnValue.close();
                }
            } finally {
                cs.close();
            }
        } finally {
            c.close();
        }

        Operation operation = assertJdbcOperation(sql);
        assertSqlParams(operation, strParam);
    }

    @Test
    public void testOperationCollectionForCallableStatementWithNamedParameters() throws SQLException {
        final String sql = "{call testSp(?)}", paramName = "@p1", paramValue = "someValue";
        Connection c = dataSource.getConnection();
        try {
            CallableStatement cs = c.prepareCall(sql);

            // So finally, figured out how hsqldb maps named parameters (@p1, @p2 etc.)
            try {
                cs.setString(paramName, paramValue);
                ResultSet returnValue = cs.executeQuery();
                if (returnValue != null) {
                    returnValue.close();
                }
            } finally {
                cs.close();
            }
        } finally {
            c.close();
        }

        Operation operation = assertJdbcOperation(sql);

        OperationMap parameters = operation.get(JdbcOperationFinalizer.PARAMS_VALUES, OperationMap.class);
        assertNotNull("Missing parameters map", parameters);
        assertEquals("Mismatched parameters count", 1, parameters.size());
        assertEquals("Mismatched parameter value", paramValue, parameters.get(paramName));
    }

    protected OperationList assertSqlParams(Operation op, String... values) {
        OperationList parameters = op.get(JdbcOperationFinalizer.PARAMS_VALUES, OperationList.class);
        assertNotNull("Missing parameters list", parameters);
        assertEquals("Mismatched parameters count", ArrayUtil.length(values), parameters.size());

        for (int index = 0; index < parameters.size(); index++) {
            assertEquals("Mismatched parameter at index=" + index, values[index], StringUtil.safeToString(parameters.get(index)));
        }
        return parameters;
    }

    @Override
    public JdbcPreparedStatementOperationCollectionAspect getAspect() {
        return JdbcPreparedStatementOperationCollectionAspect.aspectOf();
    }
}
