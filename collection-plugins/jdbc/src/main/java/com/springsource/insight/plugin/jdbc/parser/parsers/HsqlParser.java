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

import java.util.Arrays;
import java.util.List;

import com.springsource.insight.plugin.jdbc.parser.DatabaseType;
import com.springsource.insight.plugin.jdbc.parser.JdbcUrlMetaData;
import com.springsource.insight.plugin.jdbc.parser.JdbcUrlParser;
import com.springsource.insight.plugin.jdbc.parser.SimpleJdbcUrlMetaData;

public class HsqlParser implements JdbcUrlParser {

    private static final String CONNECTION_TYPE = "connection_type";

    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String DATABASE = "database";
    private static final int HSQL_DEFAULT_PORT = 9001;

    public List<JdbcUrlMetaData> parse(final String connectionUrl, String vendorName) {

        // true - has prefix (jdbc:hsqldb:)
        final HsqlProperties hsqlProps = DatabaseURL.parseURL(connectionUrl, true);
        final String type = hsqlProps.getProperty(CONNECTION_TYPE);

        final String host = (DatabaseURL.isInProcessDatabaseType(type) ? "localhost" : hsqlProps.getProperty(HOST));
        final int port = hsqlProps.getIntegerProperty(PORT, HSQL_DEFAULT_PORT);
        final String dbName = hsqlProps.getProperty(DATABASE);

        JdbcUrlMetaData simpleJdbcUrlMetaData = new SimpleJdbcUrlMetaData(host, port, dbName, connectionUrl, vendorName);
        return Arrays.asList(simpleJdbcUrlMetaData);
    }

    public DatabaseType getType() {
        return DatabaseType.HSQLDB;
    }
}
