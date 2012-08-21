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

import com.springsource.insight.util.StringUtil;

public abstract class AbstractSqlParser implements JdbcUrlParser {
    private final String defaultDBName;
    private final String defaultHost;
    private final int defaultPort;
    private final String vendorName;
    
    protected AbstractSqlParser(String vendor) {
        this(vendor, DEFAULT_DB_NAME);
    }
    
    protected AbstractSqlParser(String vendor, String dbName) {
        this(vendor, dbName, DEFAULT_HOST, DEFAULT_PORT);
    }

    protected AbstractSqlParser(String vendor, int port) {
        this(vendor, DEFAULT_HOST, port);
    }

    protected AbstractSqlParser(String vendor, String host, int port) {
    	this(vendor, DEFAULT_DB_NAME, host, port);
    }
    
    protected AbstractSqlParser(String vendor, String dbName, String host, int port) {
    	if (StringUtil.isEmpty(vendor)) {
    		throw new IllegalStateException("No vendor name specified");
    	}

    	vendorName = vendor;
        defaultDBName = dbName;
        defaultHost = host;
        defaultPort = port;
    }

    public String getVendorName() {
		return vendorName;
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
    		return (-1);	// signal the error
    	}
    }
}
