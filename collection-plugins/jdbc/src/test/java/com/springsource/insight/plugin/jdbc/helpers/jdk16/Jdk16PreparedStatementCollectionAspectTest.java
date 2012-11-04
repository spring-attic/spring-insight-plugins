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
package com.springsource.insight.plugin.jdbc.helpers.jdk16;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.springsource.insight.plugin.jdbc.JdbcPreparedStatementOperationCollectionAspect;
import com.springsource.insight.plugin.jdbc.JdbcStatementOperationCollectionTestSupport;
import com.springsource.insight.plugin.jdbc.helpers.AbstractConnection;
import com.springsource.insight.plugin.jdbc.helpers.AbstractStatement;

/**
 * 
 */
public class Jdk16PreparedStatementCollectionAspectTest extends JdbcStatementOperationCollectionTestSupport {
	private final AtomicInteger	callsCount=new AtomicInteger(0);
	private final AbstractStatement	metaDataStmt=new Jdk16Statement();
	private final DatabaseMetaData	metaData=new Jdk16DatabaseMetaData() {
			@SuppressWarnings("synthetic-access")
			@Override
			public String getURL() throws SQLException {
				callsCount.incrementAndGet();
				Connection	c=metaDataStmt.getConnection();
				try {
					CallableStatement	cs=c.prepareCall("SHOW META DATA");
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
	private final AbstractConnection	metaDataConn=new Jdk16Connection() {
			@SuppressWarnings("synthetic-access")
			@Override
			public DatabaseMetaData getMetaData() throws SQLException {
				return metaData;
			}
		};
	private final AbstractStatement	testStmt=new Jdk16Statement();

	public Jdk16PreparedStatementCollectionAspectTest() {
		metaDataStmt.setConnection(metaDataConn);
		metaDataConn.setStatement(testStmt);
		testStmt.setConnection(metaDataConn);
	}

	@Before
	@Override
	public void setUp () {
		super.setUp();
		callsCount.set(0);
	}

	@Test
    public void testPreparedGetMetadataRunsSqlQuery() throws SQLException {
		final String sql = "select * from appointment where owner = 'Agim' and dateTime = '2009-06-01'";
		PreparedStatement	ps=metaDataConn.prepareStatement(sql);
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
		CallableStatement	ps=metaDataConn.prepareCall(sql);
		try {
			ps.executeQuery();
		} finally {
			ps.close();
		}

    	assertEquals("Mismatched meta data calls count", 1, callsCount.intValue());
    	assertJdbcOperation(sql);
	}

    @Override
    public JdbcPreparedStatementOperationCollectionAspect getAspect() {
        return JdbcPreparedStatementOperationCollectionAspect.aspectOf();
    }
}
