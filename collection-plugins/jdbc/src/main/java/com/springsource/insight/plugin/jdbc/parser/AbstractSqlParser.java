/**
 * Copyright 2009-2010 the original author or authors.
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

public abstract class AbstractSqlParser implements JdbcUrlParser {
    protected static final String DEFAULT_HOST = "localhost";
    protected static final String DEFAULT_DB_NAME = "";
    protected static final int DEFAULT_PORT = -1;
    
    private final String defaultDBName;
    private final String defaultHost;
    private final int defaultPort;
    
    public AbstractSqlParser() {
        this(DEFAULT_DB_NAME, DEFAULT_HOST, DEFAULT_PORT);
    }
    
    public AbstractSqlParser(@SuppressWarnings("hiding") String defaultDBName) {
        this(defaultDBName, DEFAULT_HOST, DEFAULT_PORT);
    }
    
    public AbstractSqlParser(int port) {
        this(DEFAULT_DB_NAME, DEFAULT_HOST, port);
    }
    
    @SuppressWarnings("hiding")
    public AbstractSqlParser(String defaultDBName, String defaultHost, int defaultPort) {
        this.defaultDBName = defaultDBName;
        this.defaultHost = defaultHost;
        this.defaultPort = defaultPort;
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
    
}
