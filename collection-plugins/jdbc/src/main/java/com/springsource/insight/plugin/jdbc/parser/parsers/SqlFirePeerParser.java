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

public class SqlFirePeerParser extends AbstractSqlParser {
    
    private static final Pattern LOCATORS = Pattern.compile(".*locators=.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern LOCATOR = Pattern.compile("([^\\[]+)\\[(\\d+)\\]", Pattern.CASE_INSENSITIVE);
    private static final Pattern MULTICAST = Pattern.compile(".*mcast-port=(\\d+).*", Pattern.CASE_INSENSITIVE);
    
    public SqlFirePeerParser() {
        super(DEFAULT_DB_NAME, "", DEFAULT_PORT);
    }

    /**
     * jdbc:sqlfire:;locators=localhost[3340];mcast-port=0;host-data=false
     * jdbc:sqlfire:;mcast-port=33666;host-data=false
     */
    public List<JdbcUrlMetaData> parse(String connectionUrl, String vendorName) {
        List<JdbcUrlMetaData> parsedUrls = new ArrayList<JdbcUrlMetaData>();
        
        Matcher locatorMatcher = LOCATORS.matcher(connectionUrl);
        Matcher multiMatcher   = MULTICAST.matcher(connectionUrl);
        
        if (locatorMatcher.matches()) {
            String[] parts = connectionUrl.split(";");
            
            for(String part : parts) {
                if (part.indexOf("locators=") > -1) {
                    String[] innerParts = part.split("=");
                    if (innerParts.length > 1) {
                        String[] locators = innerParts[1].split(",");
                        
                        for(String locator : locators) {
                            Matcher m = LOCATOR.matcher(locator);
                            if (m.matches()) {
                                JdbcUrlMetaData simpleJdbcUrlMetaData = new SimpleJdbcUrlMetaData(m.group(1), Integer.parseInt(m.group(2)), 
                                        null, connectionUrl, vendorName);
                                parsedUrls.add(simpleJdbcUrlMetaData);
                            }
                        }
                    }
                    break;
                }
            }
        } else if (multiMatcher.find() && multiMatcher.groupCount() == 1) {
            int port = Integer.parseInt(multiMatcher.group(1));
            if (port != 0) {
                JdbcUrlMetaData simpleJdbcUrlMetaData = new SimpleJdbcUrlMetaData("", port, null, connectionUrl, vendorName);
                parsedUrls.add(simpleJdbcUrlMetaData);
            }
        }
        
        return parsedUrls.isEmpty() ? null : parsedUrls;
    }
}
