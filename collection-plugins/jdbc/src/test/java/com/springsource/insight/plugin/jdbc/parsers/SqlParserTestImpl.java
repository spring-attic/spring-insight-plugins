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
package com.springsource.insight.plugin.jdbc.parsers;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.springsource.insight.plugin.jdbc.parser.DatabaseType;
import com.springsource.insight.plugin.jdbc.parser.JdbcUrlMetaData;
import com.springsource.insight.plugin.jdbc.parser.JdbcUrlParser;
import com.springsource.insight.plugin.jdbc.parser.SimpleJdbcUrlMetaData;


public abstract class SqlParserTestImpl implements SqlParsetTest {

	protected JdbcUrlParser parser;
	protected List<SqlTestEntry> testCases = new ArrayList<SqlParserTestImpl.SqlTestEntry>();

	public SqlParserTestImpl() {
		super();
	}

	public class SqlTestEntry {

		private final String connectionUrl;
		private final String host;
		private final int port;
		private final String dbname;

		public SqlTestEntry(final String dbConnectionURL,
				            final String dbHost,
				            final int dbPort,
				            final String dbName) {
			this.connectionUrl = dbConnectionURL;
			this.host = dbHost;
			this.port = dbPort;
			this.dbname = dbName;
		}

		public String getConnectionUrl() {
			return connectionUrl;
		}

		public int getPort() {
			return port;
		}

		public String getHost() {
			return host;
		}

		public String getDbname() {
			return dbname;
		}
	}

	public abstract DatabaseType getType();

	@Test
	public void test() throws Exception {

		for (SqlTestEntry testEntry : testCases) {			
			final String connectionUrl1 = testEntry.getConnectionUrl();
			final List<JdbcUrlMetaData> jdbcUrlMetaData = parser.parse(connectionUrl1, getType().getVendorName());
			final SimpleJdbcUrlMetaData expected = new SimpleJdbcUrlMetaData(testEntry.getHost(),
					testEntry.getPort(),
					testEntry.getDbname(),
					testEntry.getConnectionUrl(),
					getType().getVendorName());		

			assertEquals(Arrays.asList(expected), jdbcUrlMetaData);
		}
	}
}