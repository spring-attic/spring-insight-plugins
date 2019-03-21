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
package com.springsource.insight.plugin.jdbc.parser.parsers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.springsource.insight.plugin.jdbc.parser.AbstractSqlParser;
import com.springsource.insight.plugin.jdbc.parser.JdbcUrlMetaData;
import com.springsource.insight.plugin.jdbc.parser.SimpleJdbcUrlMetaData;

public class SqlFirePeerParser extends AbstractSqlParser {
    public static final String	VENDOR="sqlfire";
    private static final Pattern LOCATORS = Pattern.compile(".*locators=.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern LOCATOR = Pattern.compile("([^\\[]+)\\[(\\d+)\\]", Pattern.CASE_INSENSITIVE);
    private static final Pattern MULTICAST = Pattern.compile(".*mcast-port=(\\d+).*", Pattern.CASE_INSENSITIVE);
    
    public SqlFirePeerParser() {
        super(VENDOR);
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
                            	String	host=m.group(1).trim();
                            	int		port=parsePort(connectionUrl, m.group(2).trim());
                                parsedUrls.add(new SimpleJdbcUrlMetaData(host, port, null, connectionUrl, vendorName));
                            }
                        }
                    }
                    break;
                }
            }
        } else if (multiMatcher.find() && multiMatcher.groupCount() == 1) {
            int port = parsePort(connectionUrl, multiMatcher.group(1).trim());
            if (port != 0) {
                JdbcUrlMetaData simpleJdbcUrlMetaData = new SimpleJdbcUrlMetaData("", port, null, connectionUrl, vendorName);
                parsedUrls.add(simpleJdbcUrlMetaData);
            }
        }
        
        return parsedUrls.isEmpty() ? null : parsedUrls;
    }
}
