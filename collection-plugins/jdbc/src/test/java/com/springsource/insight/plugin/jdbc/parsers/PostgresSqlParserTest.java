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
import com.springsource.insight.plugin.jdbc.parser.parsers.PostgresSqlParser;

/**
 * 
 */
public class PostgresSqlParserTest extends SqlParserTestImpl<PostgresSqlParser> {
	public PostgresSqlParserTest () {
		super(DatabaseType.POSTGRESQL, new PostgresSqlParser(),
			  new SqlTestEntry("jdbc:postgresql://neptune.acme.com/test",
					  		   "neptune.acme.com",
					  		   PostgresSqlParser.DEFAULT_CONNECTION_PORT,
					  		   "test"),
			  new SqlTestEntry("jdbc:postgresql://neptune.acme.com:7365/test",
					  		   "neptune.acme.com",
					  		   7365,
					  		   "test"),
			  new SqlTestEntry("jdbc:postgresql://neptune.acme.com",
					  		   "neptune.acme.com",
					  		   PostgresSqlParser.DEFAULT_CONNECTION_PORT,
					  		   JdbcUrlParser.DEFAULT_DB_NAME),
			  new SqlTestEntry("jdbc:postgresql://:7365/test",
					  		   JdbcUrlParser.DEFAULT_HOST,
					  		   7365,
					  		   "test"),
			  new SqlTestEntry("jdbc:postgresql:test",
					  		   JdbcUrlParser.DEFAULT_HOST,
					  		   PostgresSqlParser.DEFAULT_CONNECTION_PORT,
					  		   "test"),
			   new SqlTestEntry("jdbc:postgresql://host:7365/database?user=userName&password=pass",
					   		    "host",
					   		    7365,
					   		    "database"),
			   new SqlTestEntry("jdbc:postgresql://host/database?charSet=LATIN1&compatible=7.2",
			   		    	    "host",
					  		     PostgresSqlParser.DEFAULT_CONNECTION_PORT,
			   		    		 "database"));
	}
}
