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

package com.springsource.insight.plugin.jdbc.parser.parsers;

import java.util.Collections;
import java.util.List;

import com.springsource.insight.plugin.jdbc.parser.AbstractSqlParser;
import com.springsource.insight.plugin.jdbc.parser.JdbcUrlMetaData;
import com.springsource.insight.plugin.jdbc.parser.SimpleJdbcUrlMetaData;
import com.springsource.insight.util.StringUtil;

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
public class PostgresSqlParser extends AbstractSqlParser {
	public static final int	DEFAULT_CONNECTION_PORT=5432;
	private static final String	URL_PREFIX="jdbc:postgresql:";
	private static final String	HOST_DATA_PREFIX="//";

	public PostgresSqlParser () {
		super(DEFAULT_CONNECTION_PORT);
	}

	public List<JdbcUrlMetaData> parse(String connectionUrl, String vendorName) {
		if (StringUtil.isEmpty(connectionUrl) || (!connectionUrl.startsWith(URL_PREFIX))) {
			return null;
		}

		String	url=connectionUrl.substring(URL_PREFIX.length());	// strip the fixed prefix
		if (StringUtil.isEmpty(url)) {
			return null;
		}

		JdbcUrlMetaData	metaData=url.startsWith(HOST_DATA_PREFIX)
				? parseWithHostAndPort(connectionUrl, url.substring(HOST_DATA_PREFIX.length()), vendorName)
				: parseDatabaseName(connectionUrl, DEFAULT_HOST, DEFAULT_CONNECTION_PORT, vendorName, url)
				;
		if (metaData == null) {
			return null;
		} else {
			return Collections.singletonList(metaData);
		}
	}
	
	JdbcUrlMetaData parseWithHostAndPort (String connectionUrl, String url, String vendorName) {
		if (StringUtil.isEmpty(url)) {
			return null;
		}

		int		dbNamePos=url.indexOf('/');
		String	hostAndPort=(dbNamePos > 0) ? url.substring(0, dbNamePos) : url;
		int		portSep=hostAndPort.indexOf(':');
		String	host=(portSep >= 0) ? hostAndPort.substring(0, portSep) : hostAndPort;
		if (StringUtil.isEmpty(host)) {
			host = DEFAULT_HOST;
		}

		int	port=DEFAULT_CONNECTION_PORT;
		if ((portSep >= 0) && (portSep < (hostAndPort.length() - 1))) {
			String	portValue=hostAndPort.substring(portSep + 1);
			port = parsePort(connectionUrl, portValue);
		}

		if ((dbNamePos <= 0) || (dbNamePos >= (url.length() - 1))) {
			return new SimpleJdbcUrlMetaData(host, port, DEFAULT_DB_NAME, connectionUrl, vendorName);
		} else {
			return parseDatabaseName(connectionUrl, host, port, vendorName, url.substring(dbNamePos + 1));
		}
	}

	static JdbcUrlMetaData parseDatabaseName (String connectionUrl, String host, int port, String vendorName, String url) {
		if (StringUtil.isEmpty(url)) {
			return null;
		}

		int		paramsPos=url.indexOf('?');
		String	dbName=(paramsPos > 0) ? url.substring(0, paramsPos) : url;
		return new SimpleJdbcUrlMetaData(host, port, dbName, connectionUrl, vendorName);
	}
}
