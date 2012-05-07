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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.springsource.insight.plugin.jdbc.parser.AbstractSqlParser;
import com.springsource.insight.plugin.jdbc.parser.JdbcUrlMetaData;
import com.springsource.insight.plugin.jdbc.parser.SimpleJdbcUrlMetaData;

public class OracleRACParser extends AbstractSqlParser {

    public OracleRACParser() {
        super(1521);
    }

    public static final Pattern ORACLE_RAC_HOST_PATTERN = Pattern.compile("HOST\\s*=\\s*([^)]+)");
    public static final Pattern ORACLE_RAC_PORT_PATTERN = Pattern.compile("PORT\\s*=\\s*([0-9]+)");

    /**
     * Extract an Oracle RAC URL of the form: (DESCRIPTION... (ADDRESS = ...
     * (HOST = [HOST])(PORT = [PORT])...
     */
    public List<JdbcUrlMetaData> parse(String connectionUrl, String vendorName) {
        List<JdbcUrlMetaData> parsedUrls = new ArrayList<JdbcUrlMetaData>();

        String[] addressParts = connectionUrl.split("ADDRESS");
        for (String part : addressParts) {
            Matcher hostMat = ORACLE_RAC_HOST_PATTERN.matcher(part);
            Matcher portMat = ORACLE_RAC_PORT_PATTERN.matcher(part);
            if (!hostMat.find()) {
                // if there isn't a host available we can't do a thing
                continue;
            }
            String host = hostMat.group(1);
            int port = -1;
            if (portMat.find()) {
                port = Integer.parseInt(portMat.group(1));
            }

            JdbcUrlMetaData simpleJdbcUrlMetaData = new SimpleJdbcUrlMetaData(host, port, null, connectionUrl, vendorName);
            parsedUrls.add(simpleJdbcUrlMetaData);
        }

        return parsedUrls;
    }
}
