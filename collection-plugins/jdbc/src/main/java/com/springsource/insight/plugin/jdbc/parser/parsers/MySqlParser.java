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

/**
 * taken from
 * http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-configuration
 * -properties.html
 * 
 * jdbc:mysql://[host][,failoverhost...][:port]/[database] »
 * [?propertyName1][=propertyValue1][&propertyName2][=propertyValue2]...
 * 
 */
public class MySqlParser extends AbstractSqlPatternParser {
	public static final int	DEFAULT_CONNECTION_PORT=3306;
	public static final String	VENDOR="mysql";

    public MySqlParser() {
        super(VENDOR, DEFAULT_CONNECTION_PORT, create("jdbc:mysql://(.*?)(:(.*))?/(.*?)(\\?.*)??", 1, 3, 4));
    }
}
