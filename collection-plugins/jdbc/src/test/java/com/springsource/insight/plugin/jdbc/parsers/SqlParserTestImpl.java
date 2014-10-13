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
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.springsource.insight.plugin.jdbc.parser.DatabaseType;
import com.springsource.insight.plugin.jdbc.parser.JdbcUrlMetaData;
import com.springsource.insight.plugin.jdbc.parser.JdbcUrlParser;
import com.springsource.insight.plugin.jdbc.parser.SimpleJdbcUrlMetaData;
import com.springsource.insight.util.ArrayUtil;


public abstract class SqlParserTestImpl<P extends JdbcUrlParser> extends Assert {
    protected final P parser;
    protected final DatabaseType databaseType;
    private final Collection<SqlTestEntry> testCases;

    protected SqlParserTestImpl(DatabaseType dbType, P parserInstance, SqlTestEntry... testEntries) {
        this(dbType, parserInstance, (ArrayUtil.length(testEntries) <= 0) ? Collections.<SqlTestEntry>emptyList() : Arrays.asList(testEntries));
    }

    protected SqlParserTestImpl(DatabaseType dbType, P parserInstance, Collection<SqlTestEntry> testEntries) {
        if ((databaseType = dbType) == null) {
            throw new IllegalStateException("No database type specified");
        }

        if ((parser = parserInstance) == null) {
            throw new IllegalStateException("No parser instance provided");
        }

        assertEquals("Mismatched vendor name for " + dbType, dbType.getVendorName(), parserInstance.getVendorName());
        testCases = (testEntries == null) ? Collections.<SqlTestEntry>emptyList() : testEntries;
    }

    @Test
    public void testTestCases() throws Exception {
        final String vendorName = databaseType.getVendorName();
        for (SqlTestEntry testEntry : testCases) {
            final String connectionUrl = testEntry.getConnectionUrl();
            final List<JdbcUrlMetaData> metaDataList = parser.parse(connectionUrl, vendorName);
            assertNotNull("No result for " + connectionUrl, metaDataList);
            assertEquals("Multiple results for " + connectionUrl + ": " + metaDataList, 1, metaDataList.size());

            JdbcUrlMetaData actual = metaDataList.get(0);
            JdbcUrlMetaData expected = new SimpleJdbcUrlMetaData(testEntry.getHost(),
                    testEntry.getPort(),
                    testEntry.getDbname(),
                    connectionUrl,
                    vendorName);

            assertEquals("Mismatched results for " + connectionUrl, expected, actual);
        }
    }
}