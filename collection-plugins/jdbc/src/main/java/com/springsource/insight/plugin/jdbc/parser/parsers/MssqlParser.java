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
package com.springsource.insight.plugin.jdbc.parser.parsers;

import com.springsource.insight.plugin.jdbc.parser.AbstractSqlPatternParser;

public class MssqlParser extends AbstractSqlPatternParser {
    private static final String EXTRA_PATTERN = "//(.+)?:([^;]+)?;.*[Dd]atabaseName=([^;]+)?.*";
    public static final int DEFAULT_CONNECTION_PORT = 1433;
    public static final String VENDOR = "microsoft", SUB_TYPE = "sqlserver";

    public MssqlParser() {
        super(VENDOR, DEFAULT_CONNECTION_PORT, create(VENDOR, SUB_TYPE, EXTRA_PATTERN, 1, 2, 3)
                , create(JDBC_PREFIX + ":" + SUB_TYPE + ":" + EXTRA_PATTERN, 1, 2, 3));
    }
}
