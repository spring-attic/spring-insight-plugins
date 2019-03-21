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
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.springsource.insight.plugin.jdbc.parser.AbstractSqlParser;
import com.springsource.insight.plugin.jdbc.parser.JdbcUrlMetaData;
import com.springsource.insight.plugin.jdbc.parser.SimpleJdbcUrlMetaData;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.StringUtil;

public class OracleRACParser extends AbstractSqlParser {
	public static final int	DEFAULT_CONNECTION_PORT=1521;
	public static final String DEFAULT_CONNECTION_PORT_STRING=String.valueOf(DEFAULT_CONNECTION_PORT);
	public static final String	VENDOR="oracle";

	public OracleRACParser() {
        super(VENDOR, DEFAULT_CONNECTION_PORT);
    }

    public static final String	HOST_ATTRIBUTE="HOST", PORT_ATTRIBUTE="PORT", SERVICE_ATTRIBUTE="SERVICE_NAME";
    public static final Pattern ORACLE_RAC_HOST_PATTERN = createDefaultAttributePattern(HOST_ATTRIBUTE);
    public static final Pattern ORACLE_RAC_PORT_PATTERN = createDefaultAttributePattern(PORT_ATTRIBUTE);
    public static final Pattern ORACLE_RAC_SERVICE_PATTERN = createDefaultAttributePattern(SERVICE_ATTRIBUTE);

    /**
     * Extract an Oracle RAC URL of the form: (DESCRIPTION... (ADDRESS = ...
     * (HOST = [HOST])(PORT = [PORT])...
     */
    public List<JdbcUrlMetaData> parse(String connectionUrl, String vendorName) {
        String[] addressParts = connectionUrl.split("ADDRESS");
        /*
         * NOTE: the 1st address "part" is actually everything BEFORE the 1st
         * address and as such contains no useful information
         */
        if (ArrayUtil.length(addressParts) <= 1) {
        	return Collections.emptyList();
        }

        String	dbName = resolveAttributeValue(SERVICE_ATTRIBUTE, ORACLE_RAC_SERVICE_PATTERN, connectionUrl, null);
        List<JdbcUrlMetaData> parsedUrls = new ArrayList<JdbcUrlMetaData>(addressParts.length - 1);
        for (int	index=1; index < addressParts.length; index++) {
        	String part = addressParts[index];
        	String host = resolveAttributeValue(HOST_ATTRIBUTE, ORACLE_RAC_HOST_PATTERN, part, getDefaultHost());
        	String portValue = resolveAttributeValue(PORT_ATTRIBUTE, ORACLE_RAC_PORT_PATTERN, part, DEFAULT_CONNECTION_PORT_STRING);
        	int    port = getDefaultPort();
            if (portValue != DEFAULT_CONNECTION_PORT_STRING) {
            	port = parsePort(connectionUrl, portValue);
            }

            parsedUrls.add(new SimpleJdbcUrlMetaData(host, port, dbName, connectionUrl, vendorName));
        }

        return parsedUrls;
    }

    static String resolveAttributeValue (String attrName, Pattern pattern, String part, String defaultValue) {
    	if (StringUtil.isEmpty(part) || (!part.contains(attrName))) {
    		return defaultValue;
    	}

    	Matcher	attrMat=pattern.matcher(part);
    	if (attrMat.find()) {
    		String	attrValue=attrMat.group(1);
    		if (attrValue != null) {
    			attrValue = attrValue.trim();
    		}

    		if (StringUtil.isEmpty(attrValue)) {
    			return defaultValue;
    		} else {
    			return attrValue;
    		}
    	} else {
    		return defaultValue;
    	}
    }

    static final Pattern createDefaultAttributePattern (String attrName) {
    	return Pattern.compile(attrName +  "\\s*=\\s*([^)]+)");	
    }
}
