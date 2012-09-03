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


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;

/**
 * Verifies that a common use case (param logging JDBC statements) works
 */
@ContextConfiguration("classpath:jdbc-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class ParamLoggingPreparedStatementTest
    	extends OperationCollectionAspectTestSupport {
    @Autowired DataSource dataSource;

    public ParamLoggingPreparedStatementTest () {
    	super();
    }

    @Test
    public void operationCollectionForPreparedStatement() throws SQLException {
        Connection c = dataSource.getConnection();
        String sql = "select * from appointment where owner = ?";
        final PreparedStatement ps = c.prepareStatement(sql);
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("setString")) {
                    ps.setString(1, "Agim");
                    return null;
                } else if (method.getName().equals("execute")) {
                    return Boolean.valueOf(ps.execute());
                } else {
                    throw new RuntimeException("foo");
                } 
            }
        };
        PreparedStatement delegator = (PreparedStatement) Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(), 
                                                                                 new Class[] { PreparedStatement.class },
                                                                                 handler);
        
        delegator.setString(1, "Agim");
        delegator.execute();

        Operation op = getLastEntered();
        assertEquals(sql, op.get("sql"));
        OperationList parameters = op.get("params", OperationList.class);
        assertEquals(1, parameters.size());
        assertEquals("Agim", parameters.get(0));
    }
    
    @Override
    public OperationCollectionAspectSupport getAspect() {
        return JdbcPreparedStatementOperationCollectionAspect.aspectOf();
    }
}
