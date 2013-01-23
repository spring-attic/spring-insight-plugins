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

package com.springsource.insight.plugin.jdbc.parsers;

import com.springsource.insight.plugin.jdbc.parser.DatabaseType;
import com.springsource.insight.plugin.jdbc.parser.JdbcUrlParser;
import com.springsource.insight.plugin.jdbc.parser.parsers.PostgresSqlParser;

/**
 * 
 */
public class PostgresSqlParserTest extends SqlParserTestImpl<PostgresSqlParser> {
	public PostgresSqlParserTest () {
		super(DatabaseType.POSTGRESQL, new PostgresSqlParser(),
		  new SqlTestEntry("jdbc:postgresql:database",
				 JdbcUrlParser.DEFAULT_HOST,
				 PostgresSqlParser.DEFAULT_CONNECTION_PORT,
				 "database")
		 ,
		 new SqlTestEntry("jdbc:edb:database",
				 JdbcUrlParser.DEFAULT_HOST,
				 PostgresSqlParser.DEFAULT_CONNECTION_PORT,
				 "database")
		,
		new SqlTestEntry("jdbc:postgresql:data123",
				JdbcUrlParser.DEFAULT_HOST,
				PostgresSqlParser.DEFAULT_CONNECTION_PORT,
				"data123")
		,
		new SqlTestEntry("jdbc:edb:data123",
				JdbcUrlParser.DEFAULT_HOST,
				PostgresSqlParser.DEFAULT_CONNECTION_PORT,
				"data123")
		,
		new SqlTestEntry("jdbc:postgresql://[::1]/database",
				"::1",
				PostgresSqlParser.DEFAULT_CONNECTION_PORT,
				"database")
		,
		new SqlTestEntry("jdbc:edb://[::1]/database",
				"::1",
				PostgresSqlParser.DEFAULT_CONNECTION_PORT,
				"database")
		,
		new SqlTestEntry("jdbc:postgresql://[2001:0db8:85a3:0042:1000:8a2e:0370:7334]/database",
				"2001:0db8:85a3:0042:1000:8a2e:0370:7334",
				PostgresSqlParser.DEFAULT_CONNECTION_PORT,
				"database")
		,
		new SqlTestEntry("jdbc:edb://[2001:0db8:85a3:0042:1000:8a2e:0370:7334]/database",
				"2001:0db8:85a3:0042:1000:8a2e:0370:7334",
				PostgresSqlParser.DEFAULT_CONNECTION_PORT,
				"database")
		,
		new SqlTestEntry("jdbc:edb://[2001:0db8:85a3:0042:1000:8a2e:0370:7334]/database?someprop=somevalue",
				"2001:0db8:85a3:0042:1000:8a2e:0370:7334",
				PostgresSqlParser.DEFAULT_CONNECTION_PORT,
				"database")
		,
		new SqlTestEntry("jdbc:postgresql://myhost/database",
				"myhost",
				PostgresSqlParser.DEFAULT_CONNECTION_PORT,
				"database")
		,
		new SqlTestEntry("jdbc:edb://myhost/database",
				"myhost",
				PostgresSqlParser.DEFAULT_CONNECTION_PORT,
				"database")
		,
		new SqlTestEntry("jdbc:postgresql://10.23.197.110/database",
				"10.23.197.110",
				PostgresSqlParser.DEFAULT_CONNECTION_PORT,
				"database")
		,
		new SqlTestEntry("jdbc:edb://10.23.197.110/database",
				"10.23.197.110",
				PostgresSqlParser.DEFAULT_CONNECTION_PORT,
				"database")
		,
		new SqlTestEntry("jdbc:edb://10.23.197.110/database?someprop=somevalue",
				"10.23.197.110",
				PostgresSqlParser.DEFAULT_CONNECTION_PORT,
				"database")
		,
		new SqlTestEntry("jdbc:postgresql://[::1]:9090/database",
				"::1",
				9090,
				"database")
		,
		new SqlTestEntry("jdbc:edb://[::1]:9090/database",
				"::1",
				9090,
				"database")
		,
		new SqlTestEntry("jdbc:postgresql://[2001:0db8:85a3:0042:1000:8a2e:0370:7334]:9090/database",
				"2001:0db8:85a3:0042:1000:8a2e:0370:7334",
				9090,
				"database")
		,
		new SqlTestEntry("jdbc:edb://[2001:0db8:85a3:0042:1000:8a2e:0370:7334]:9090/database",
				"2001:0db8:85a3:0042:1000:8a2e:0370:7334",
				9090,
				"database")
		,
		new SqlTestEntry("jdbc:edb://[2001:0db8:85a3:0042:1000:8a2e:0370:7334]:9090/database?someprop=somevalue",
				"2001:0db8:85a3:0042:1000:8a2e:0370:7334",
				9090,
				"database")
		,
		new SqlTestEntry("jdbc:postgresql://myhost:8080/database",
				"myhost",
				8080,
				"database")
		,
		new SqlTestEntry("jdbc:edb://myhost:8080/database",
				"myhost",
				8080,
				"database")
		,
		new SqlTestEntry("jdbc:postgresql://10.23.197.110:8080/database",
				"10.23.197.110",
				8080,
				"database")
		,
		new SqlTestEntry("jdbc:edb://10.23.197.110:8080/database",
				"10.23.197.110",
				8080,
				"database")
		,
		new SqlTestEntry("jdbc:edb://10.23.197.110:8080/database?someprop=somevalue",
				"10.23.197.110",
				8080,
				"database")
		);
	}
}
