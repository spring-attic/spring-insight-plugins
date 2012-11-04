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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.springsource.insight.plugin.jdbc.JdbcStatementOperationCollectionAspect;
import com.springsource.insight.plugin.jdbc.JdbcStatementOperationCollectionTestSupport;
import com.springsource.insight.plugin.jdbc.helpers.AbstractStatement;

/**
 * Special tests that use classes that mock {@link Connection}, {@link Statement}, {@link DatabaseMetaData}
 * for JDK 1.6 and above
 */
public class Jdk16StatementOperationCollectionAspectTest extends JdbcStatementOperationCollectionTestSupport {
	public Jdk16StatementOperationCollectionAspectTest () {
		super();
	}

	@Test
    public void testGetMetadataRunsSqlQuery() throws SQLException {
    	final AbstractStatement	metaDataStmt=new Jdk16Statement();
    	final AtomicInteger	callsCount=new AtomicInteger(0);
    	final DatabaseMetaData	metaData=new Jdk16DatabaseMetaData() {
			@Override
			public String getURL() throws SQLException {
				callsCount.incrementAndGet();
				return "jdbc:test:call=" + metaDataStmt.execute("SHOW META DATA");
			}
    	};
    	Connection	metaDataConn=new Jdk16Connection() {
			@Override
			public DatabaseMetaData getMetaData() throws SQLException {
				return metaData;
			}
		};
		metaDataStmt.setConnection(metaDataConn);

		AbstractStatement	orgStmt=new Jdk16Statement();
		orgStmt.setConnection(metaDataConn);

		final String sql = "select * from appointment where owner = 'Agim' and dateTime = '2009-06-01'";
    	assertTrue("Failed to executed dummy SQL", orgStmt.execute(sql));
    	assertEquals("Mismatched meta data calls count", 1, callsCount.intValue());
    	assertJdbcOperation(sql);
    }

    @Override
    public JdbcStatementOperationCollectionAspect getAspect() {
        return JdbcStatementOperationCollectionAspect.aspectOf();
    }
}
