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

package com.springsource.insight.plugin.jdbc.parser.parsers;

import com.springsource.insight.plugin.jdbc.parser.SimpleSqlUrlParser;

/**
 * <P>The following formats are supported:</P></BR>
 * 
 * 		jdbc:postgresql:database 
 *		jdbc:postgresql://host/database 
 *		jdbc:postgresql://host:port/database
 *
 * <P>If not specified, host defaults to <code>localhost</code>
 * The default port for PostgreSQL is 5432. Usually, if the default port is
 * being used by the database server, the port value of the JDBC url can be
 * omitted.</P></BR>
 * 
 * Examples:</BR>
 *
 *		jdbc:postgresql://neptune.acme.com:5432/test 
 *		jdbc:postgresql:test - equivalent to jdbc:postgresql://localhost:5432/test
 */
public class PostgresSqlParser extends SimpleSqlUrlParser {
	public static final int	DEFAULT_CONNECTION_PORT=5432;
	public static final String	VENDOR="postgresql";

	public PostgresSqlParser () {
		super(VENDOR, DEFAULT_CONNECTION_PORT);
	}
}
