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

import com.springsource.insight.plugin.jdbc.parser.AbstractSqlPatternParser;

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
public class PostgresSqlParser extends AbstractSqlPatternParser {
	/**
	 * Defines a non-capturing group for postgres or edb
	 */
	private static final String VENDOR_LIST  =  JDBC_PREFIX + ":(?:postgresql|edb)";
	
	/**
	 * Defines a group for IPv6, IPv4 IPs and host names<br/>
	 * Will match the next addresses:
	 * <ol>
	 * <li>IPv6 localhost - [::1]
	 * <li>IPv6 ip address - [2001:0db8:85a3:0042:1000:8a2e:0370:7334]
	 * </ol>
	 */
	private static final String IPv6_PATTERN =  "\\[(::1|[0-9a-f]{4}(?:[:][0-9a-f]{4}){7})\\]";
	
	/**
	 * Matches jdbc:postgres:database and jdbc:edb:database  
	 */
	private static final String PATTERN1 = VENDOR_LIST+":([^:/?]+)";
	
	/**
	 * Matches jdbc:postgres://host/database and jdbc:edb://host/database (IPv4 and host names)  
	 */
	private static final String PATTERN2 = VENDOR_LIST+"://([^:/?]+)/([^:/?]+).*";
	
	/**
	 * Matches jdbc:postgres://host:port/database and jdbc:edb://host:port/database (IPv4 and host names)  
	 */
	private static final String PATTERN3 = VENDOR_LIST+"://([^:/?]+):(\\d+)/([^:/?]+).*";
	
	/**
	 * Matches jdbc:postgres://host/database and jdbc:edb://host/database (IPv6)  
	 */
	private static final String PATTERN4 = VENDOR_LIST+"://"+IPv6_PATTERN+"/([^:/?]+).*";
	
	/**
	 * Matches jdbc:postgres://host:port/database and jdbc:edb://host:port/database (IPv6)  
	 */
	private static final String PATTERN5 = VENDOR_LIST+"://"+IPv6_PATTERN+":(\\d+)/([^:/?]+).*";
	
	public static final int	DEFAULT_CONNECTION_PORT=5432;
	public static final String	VENDOR="postgresql";

	public PostgresSqlParser () {
		super(VENDOR, DEFAULT_CONNECTION_PORT, create(PATTERN1, -1, -1, 1),
				                               create(PATTERN2, 1, -1, 2),
				                               create(PATTERN3, 1, 2, 3),
				                               create(PATTERN4, 1, -1, 2),
				                               create(PATTERN5, 1, 2, 3));
	}
}
