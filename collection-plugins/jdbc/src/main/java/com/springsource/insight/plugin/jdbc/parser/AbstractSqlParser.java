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
package com.springsource.insight.plugin.jdbc.parser;

import java.util.logging.Logger;

import com.springsource.insight.util.StringUtil;

public abstract class AbstractSqlParser implements JdbcUrlParser {
    private final String defaultDBName;
    private final String defaultHost;
    private final int defaultPort;
    
    protected AbstractSqlParser() {
        this(DEFAULT_DB_NAME, DEFAULT_HOST, DEFAULT_PORT);
    }
    
    protected AbstractSqlParser(String dbName) {
        this(dbName, DEFAULT_HOST, DEFAULT_PORT);
    }

    protected AbstractSqlParser(int port) {
        this(DEFAULT_HOST, port);
    }

    protected AbstractSqlParser(String host, int port) {
    	this(DEFAULT_DB_NAME, host, port);
    }
    
    protected AbstractSqlParser(String dbName, String host, int port) {
        this.defaultDBName = dbName;
        this.defaultHost = host;
        this.defaultPort = port;
    }

    public String getDefaultDatbaseName() {
        return defaultDBName;
    }

    public String getDefaultHost() {
        return defaultHost;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    protected int parsePort (String connectionUrl, String portValue) {
    	if (StringUtil.isEmpty(portValue)) {
    		return getDefaultPort();
    	}

    	try {
    		return Integer.parseInt(portValue);
    	} catch(NumberFormatException e) {
    		Logger	LOG=Logger.getLogger(getClass().getName());
    		LOG.warning("parsePort(" + connectionUrl + ") failed to extract port value=" + portValue + ": " + e.getMessage());
    		return (-1);
    	}
    }
}
