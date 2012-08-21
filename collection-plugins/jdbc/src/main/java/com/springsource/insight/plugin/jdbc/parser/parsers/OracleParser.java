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

import com.springsource.insight.plugin.jdbc.parser.AbstractSqlPatternParser;
import com.springsource.insight.plugin.jdbc.parser.SqlParserPattern;

public class OracleParser extends AbstractSqlPatternParser {
	public static final int	DEFAULT_CONNECTION_PORT=1521;
	public static final String	VENDOR="oracle", SUB_TYPE="thin";

	public OracleParser() {
        super(VENDOR, DEFAULT_CONNECTION_PORT,
        	  create(".*@//(.*?)(:(.*))?/(.*)"),
        	  create(".*@(.*?)(:(.*))?:(.*)"));
    }
	
	protected static final SqlParserPattern create(String pattern) {
		return create(VENDOR, SUB_TYPE, pattern, 1, 3, 4);
	}
}
