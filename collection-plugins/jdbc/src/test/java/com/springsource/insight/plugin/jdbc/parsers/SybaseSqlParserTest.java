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

import com.springsource.insight.plugin.jdbc.parser.DatabaseType;
import com.springsource.insight.plugin.jdbc.parser.JdbcUrlParser;
import com.springsource.insight.plugin.jdbc.parser.parsers.SybaseSqlParser;

/**
 * 
 */
public class SybaseSqlParserTest extends SqlParserTestImpl<SybaseSqlParser> {
	private static final String	TEST_HOST="neptune.acme.com";
	private static final int	TEST_PORT=7365;
	private static final String	TEST_DB="ryol";
	public SybaseSqlParserTest () {
		super(DatabaseType.SYBASE, new SybaseSqlParser(),
				new SqlTestEntry("jdbc:sybase:Tds:" + TEST_HOST + ":" + TEST_PORT,
						TEST_HOST,
						TEST_PORT,
						JdbcUrlParser.DEFAULT_DB_NAME),
				new SqlTestEntry("jdbc:sybase:Tds:" + TEST_HOST,
						TEST_HOST,
						JdbcUrlParser.DEFAULT_PORT,
						JdbcUrlParser.DEFAULT_DB_NAME),
				new SqlTestEntry("jdbc:sybase:Tds:" + TEST_HOST + ":" + TEST_PORT + "?ServiceName=" + TEST_DB,
						TEST_HOST,
						TEST_PORT,
						TEST_DB),
				new SqlTestEntry("jdbc:sybase:Tds:" + TEST_HOST + "?ServiceName=" + TEST_DB,
						TEST_HOST,
						JdbcUrlParser.DEFAULT_PORT,
						TEST_DB));
	}
}
