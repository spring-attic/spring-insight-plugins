/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
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
    private final String defaultPortString;
    private final String vendorName;
    private final String urlPrefix;

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
        urlPrefix = createUrlPrefix(vendor);
        defaultDBName = dbName;
        defaultHost = host;
        defaultPort = port;
        defaultPortString = String.valueOf(port);
    }

    /**
     * @return <code>jdbc:{@link #getVendorName()}:</code>
     */
    public String getUrlPrefix() {
        return urlPrefix;
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

    public String getDefaultPortString() {
        return defaultPortString;
    }

    protected String createUrlPrefix(String vendor) {
        return JDBC_PREFIX + ":" + vendor + ":";
    }

    /**
     * @param connectionUrl The original connection URL
     * @return The rest of the URL with the <code>jdbc</code> and vendor
     * part(s) stripped - <code>null</code>/empty if empty URL or
     * not starts with the required prefix
     * @see #getUrlPrefix()
     */
    protected String stripUrlPrefix(String connectionUrl) {
        String prefix = getUrlPrefix();
        if (StringUtil.isEmpty(connectionUrl) || (!connectionUrl.startsWith(prefix))) {
            return null;
        }

        return connectionUrl.substring(prefix.length());
    }

    protected int parsePort(String connectionUrl, String portValue) {
        if (StringUtil.isEmpty(portValue)) {
            return getDefaultPort();
        }

        try {
            return Integer.parseInt(portValue);
        } catch (NumberFormatException e) {
            return (-1);    // signal the error
        }
    }
}
