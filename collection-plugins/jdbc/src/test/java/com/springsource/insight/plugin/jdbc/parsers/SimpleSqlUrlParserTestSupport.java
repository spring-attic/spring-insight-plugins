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

package com.springsource.insight.plugin.jdbc.parsers;

import java.util.Arrays;
import java.util.Collection;

import com.springsource.insight.plugin.jdbc.parser.DatabaseType;
import com.springsource.insight.plugin.jdbc.parser.SimpleSqlUrlParser;

/**
 *
 */
public abstract class SimpleSqlUrlParserTestSupport<P extends SimpleSqlUrlParser> extends SqlParserTestImpl<P> {
    protected SimpleSqlUrlParserTestSupport(DatabaseType dbType, P parserInstance) {
        super(dbType, parserInstance, createDefaultTestCases(parserInstance));
    }

    protected static final Collection<SqlTestEntry> createDefaultTestCases(SimpleSqlUrlParser parser) {
        return Arrays.asList(
                new SqlTestEntry(parser.getUrlPrefix() + "//neptune.acme.com/test",
                        "neptune.acme.com",
                        parser.getDefaultPort(),
                        "test"),
                new SqlTestEntry(parser.getUrlPrefix() + "//neptune.acme.com:7365/test",
                        "neptune.acme.com",
                        7365,
                        "test"),
                new SqlTestEntry(parser.getUrlPrefix() + "//neptune.acme.com",
                        "neptune.acme.com",
                        parser.getDefaultPort(),
                        parser.getDefaultDatbaseName()),
                new SqlTestEntry(parser.getUrlPrefix() + "//:7365/test",
                        parser.getDefaultHost(),
                        7365,
                        "test"),
                new SqlTestEntry(parser.getUrlPrefix() + "test",
                        parser.getDefaultHost(),
                        parser.getDefaultPort(),
                        "test"),
                new SqlTestEntry(parser.getUrlPrefix() + "//host:7365/database?user=userName&password=pass",
                        "host",
                        7365,
                        "database"),
                new SqlTestEntry(parser.getUrlPrefix() + "//host/database?charSet=LATIN1&compatible=7.2",
                        "host",
                        parser.getDefaultPort(),
                        "database"));
    }
}
