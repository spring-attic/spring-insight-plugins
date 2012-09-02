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

package com.springsource.insight.plugin.jdbc.parser;

import java.util.Collections;
import java.util.List;

import com.springsource.insight.util.StringUtil;

/**
 * <P>Parses JDBC URL(s) of the format(s):</P></BR>
 * <UL>
 * 		<LI>jdbc:vendor://host:port/dbName?options</LI> 
 *		<LI>jdbc:postgresql:dbName - equivalent to jdbc:postgresql://default-host:default-port/dbName</LI>
 * </UL>
 */
public abstract class SimpleSqlUrlParser extends AbstractSqlParser {
	public static final String	HOST_DATA_PREFIX="//";

	protected SimpleSqlUrlParser(String vendor) {
		this(vendor, DEFAULT_DB_NAME);
	}

	protected SimpleSqlUrlParser(String vendor, String dbName) {
		this(vendor, dbName, DEFAULT_HOST, DEFAULT_PORT);
	}

	protected SimpleSqlUrlParser(String vendor, int port) {
		this(vendor, DEFAULT_HOST, port);
	}

	protected SimpleSqlUrlParser(String vendor, String host, int port) {
		this(vendor, DEFAULT_DB_NAME, host, port);
	}

	protected SimpleSqlUrlParser(String vendor, String dbName, String host, int port) {
		super(vendor, dbName, host, port);
	}

	public List<JdbcUrlMetaData> parse(String connectionUrl, String vendorName) {
		String	url=stripUrlPrefix(connectionUrl);
		if (StringUtil.isEmpty(url)) {
			return null;
		}

		JdbcUrlMetaData	metaData=url.startsWith(HOST_DATA_PREFIX)
				? parseWithHostAndPort(connectionUrl, url.substring(HOST_DATA_PREFIX.length()), vendorName)
				: parseDatabaseName(connectionUrl, DEFAULT_HOST, getDefaultPort(), vendorName, url)
				;
		if (metaData == null) {
			return null;
		} else {
			return Collections.singletonList(metaData);
		}
	}

	protected JdbcUrlMetaData parseWithHostAndPort (String connectionUrl, String url, String vendorName) {
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

		int	port=getDefaultPort();
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

	protected JdbcUrlMetaData parseDatabaseName (String connectionUrl, String host, int port, String vendorName, String url) {
		if (StringUtil.isEmpty(url)) {
			return null;
		}

		int		paramsPos=url.indexOf('?');
		if (paramsPos < 0) {	// some URL(s) use ';' for extra attributes instead of '?'
			paramsPos=url.indexOf(';');
		}
		String	dbName=(paramsPos > 0) ? url.substring(0, paramsPos) : url;
		return new SimpleJdbcUrlMetaData(host, port, dbName, connectionUrl, vendorName);
	}
}
