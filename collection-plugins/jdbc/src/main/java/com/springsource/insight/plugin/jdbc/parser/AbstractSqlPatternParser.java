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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.StringUtil;

public abstract class AbstractSqlPatternParser extends AbstractSqlParser {

    private final SqlParserPattern[] patterns;

    protected AbstractSqlPatternParser(String vendor, SqlParserPattern... patternValues) {
        this(vendor, DEFAULT_DB_NAME, patternValues);
    }

    protected AbstractSqlPatternParser(String vendor, String dbName, SqlParserPattern... patternValues) {
        this(vendor, dbName, DEFAULT_HOST, DEFAULT_PORT, patternValues);
    }

    protected AbstractSqlPatternParser(String vendor, int port, SqlParserPattern... patternValues) {
        this(vendor, DEFAULT_HOST, port, patternValues);
    }

    protected AbstractSqlPatternParser(String vendor, String host, int port, SqlParserPattern... patternValues) {
        this(vendor, DEFAULT_DB_NAME, host, port, patternValues);
    }

    protected AbstractSqlPatternParser(String vendor, String dbName, String host, int port, SqlParserPattern... patternValues) {
        super(vendor, dbName, host, port);

        if (ArrayUtil.length(patternValues) <= 0) {
            throw new IllegalStateException("No patterns specified");
        }

        this.patterns = patternValues;
    }

    public List<JdbcUrlMetaData> parse(String connectionUrl, String vendorName) {
        for (final SqlParserPattern sqlParserPattern : patterns) {
            final Pattern urlPattern = sqlParserPattern.getCompiledPattern();
            final Matcher urlMatcher = urlPattern.matcher(connectionUrl);
            if (!urlMatcher.matches()) {
                continue;
            }

            final String host = getHost(connectionUrl, urlMatcher, sqlParserPattern);
            final String port = getPort(connectionUrl, urlMatcher, sqlParserPattern);
            final String databaseName = getDBName(connectionUrl, urlMatcher, sqlParserPattern);
            final int finalPort = parsePort(connectionUrl, port);

            return Collections.<JdbcUrlMetaData>singletonList(new SimpleJdbcUrlMetaData(host, finalPort, databaseName, connectionUrl, vendorName));
        }

        return null;
    }

    protected String getHost(String connectionUrl, Matcher matcher, SqlParserPattern pattern) {
        return getValue(connectionUrl, matcher, pattern.getHostIndex(), getDefaultHost());
    }

    protected String getPort(String connectionUrl, Matcher matcher, SqlParserPattern pattern) {
        return getValue(connectionUrl, matcher, pattern.getPortIndex(), getDefaultPortString());
    }

    protected String getDBName(String connectionUrl, Matcher matcher, SqlParserPattern pattern) {
        return getValue(connectionUrl, matcher, pattern.getDatabaseNameIndex(), getDefaultDatbaseName());
    }

    protected String getValue(String connectionUrl, Matcher matcher, int index, String defaultValue) {
        String res = getGroupValue(connectionUrl, matcher, index);
        return StringUtil.isEmpty(res) ? defaultValue : res;
    }

    protected String getGroupValue(String connectionUrl, Matcher matcher, int index) {
        if (index > -1) {
            String attrValue = matcher.group(index);
            if (attrValue != null) {
                attrValue = attrValue.trim();
            }

            return attrValue;
        } else {
            return null;
        }
    }

    protected static final SqlParserPattern create(String vendor, String subType, String extra, int hostIndex, int portIndex, int databaseNameIndex) {
        return create(vendor, subType + ":" + extra, hostIndex, portIndex, databaseNameIndex);
    }

    protected static final SqlParserPattern create(String vendor, String extra, int hostIndex, int portIndex, int databaseNameIndex) {
        return create(JDBC_PREFIX + ":" + vendor + ":" + extra, hostIndex, portIndex, databaseNameIndex);
    }

    protected static final SqlParserPattern create(String pattern, int hostIndex, int portIndex, int databaseNameIndex) {
        return new SqlParserPattern(pattern, hostIndex, portIndex, databaseNameIndex);
    }
}
