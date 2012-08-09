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

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.StringUtil;

public abstract class AbstractSqlPatternParser extends AbstractSqlParser {
    
    private final SqlParserPattern[] patterns;
    
    protected AbstractSqlPatternParser(SqlParserPattern... patternValues) {
        this(DEFAULT_DB_NAME, patternValues);
    }
    
    protected AbstractSqlPatternParser(String dbName, SqlParserPattern... patternValues) {
        this(dbName, DEFAULT_HOST, DEFAULT_PORT, patternValues);
    }
    
    protected AbstractSqlPatternParser(int port, SqlParserPattern... patternValues) {
        this(DEFAULT_HOST, port, patternValues);
    }

    protected AbstractSqlPatternParser(String host, int port, SqlParserPattern... patternValues) {
    	this(DEFAULT_DB_NAME, host, port, patternValues);
 	}

    protected AbstractSqlPatternParser(String dbName, String host, int port,SqlParserPattern... patternValues) {
        super(dbName, host, port);

		if (ArrayUtil.length(patternValues) <= 0) {
			throw new IllegalStateException("No patterns specified");
		}

        this.patterns = patternValues;
    }

    public List<JdbcUrlMetaData> parse(final String connectionUrl, final String vendorName) {
        for (final SqlParserPattern sqlParserPattern : patterns) {
            final Pattern urlPattern = sqlParserPattern.getCompiledPattern();
            final Matcher urlMatcher = urlPattern.matcher(connectionUrl);

            if (!urlMatcher.matches()) {
                continue;
            }

            final String host = getHost(urlMatcher, sqlParserPattern);
            final String port = getPort(urlMatcher, sqlParserPattern);
            final String databaseName = getDBName(urlMatcher, sqlParserPattern);

            int finalPort;

            try {
                finalPort = Integer.parseInt(port);
            } catch (final NumberFormatException exception) {
                return null;
            }

            JdbcUrlMetaData simpleJdbcUrlMetaData=new SimpleJdbcUrlMetaData(host, finalPort, databaseName, connectionUrl, vendorName);
            return Collections.singletonList(simpleJdbcUrlMetaData);
        }
        
        return null;
    }
    
    private String getHost(Matcher matcher, SqlParserPattern pattern) {
        return getValue(matcher, pattern.getHostIndex(), getDefaultHost());
    }
    
    private String getPort(Matcher matcher, SqlParserPattern pattern) {
        return getValue(matcher, pattern.getPortIndex(), String.valueOf(getDefaultPort()));
    }
    
    private String getDBName(Matcher matcher, SqlParserPattern pattern) {
        return getValue(matcher, pattern.getDatabaseNameIndex(), getDefaultDatbaseName());
    }
    
    private String getValue(Matcher matcher, int index, String defaultValue) {
        String res = getGroupValue(matcher, index);
        return StringUtil.isEmpty(res) ? defaultValue : res;
    }

    private String getGroupValue(Matcher matcher, int index) {
        String toReturn = null;
        
        if (index > -1) {
            toReturn = matcher.group(index);
        }
        
        return toReturn;
    }
    
    protected static SqlParserPattern create(final String pattern, final int hostIndex, final int portIndex, final int databaseNameIndex) {
        return new SqlParserPattern(pattern, hostIndex, portIndex, databaseNameIndex);
    }
}
