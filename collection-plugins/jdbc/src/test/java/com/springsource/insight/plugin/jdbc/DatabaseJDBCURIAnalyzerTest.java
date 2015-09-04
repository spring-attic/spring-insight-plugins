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
package com.springsource.insight.plugin.jdbc;

import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Test;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;

/**
 */
public class DatabaseJDBCURIAnalyzerTest extends AbstractDatabaseJDBCURIAnalyzerTest {
    private final DatabaseJDBCURIAnalyzer dbAnalyzer = new TestJDBCURIAnalyzer();

    public DatabaseJDBCURIAnalyzerTest() {
        super();
    }

    @Test
    public void testLocateDatabaseURI() throws Exception {
        String jdbcUri = "jdbc:foobar://huh:8080";
        String toHash = "foobar" + "" +
                "" + "huh" + 8080;
        Operation op = createJdbcOperation(jdbcUri);
        Frame frame = createJdbcFrame(op);
        Trace trace = createJdbcTrace(frame);

        List<ExternalResourceDescriptor> descList =
                (List<ExternalResourceDescriptor>) dbAnalyzer.locateExternalResourceName(trace);
        assertEquals("Mismatched num of descriptors", 1, descList.size());
        ExternalResourceDescriptor externalResourceDescriptor = descList.get(0);

        assertEquals("Mismatched extracted frame", frame, externalResourceDescriptor.getFrame());
        assertEquals("Mismatched resource type", ExternalResourceType.DATABASE.name(), externalResourceDescriptor.getType());
        assertEquals("Mismatched resource name", "foobar:1:" + MD5NameGenerator.getName(toHash), externalResourceDescriptor.getName());
        assertEquals("Mismatched incoming value", Boolean.FALSE, Boolean.valueOf(externalResourceDescriptor.isIncoming()));
    }

    @Test
    public void test_ExtractMeaningfulNames_ParserReturnsOne() throws Exception {
        Frame frame = mock(Frame.class);
        String testUri = "jdbc:mysql://chef.metadyne.com:3000/adk?user=username&password=test";
        List<ExternalResourceDescriptor> result = dbAnalyzer.extractMeaningfulNames(frame, testUri);
        String toHash = "mysql" + "adk" + "chef.metadyne.com" + 3000;
        assertEquals(1, result.size());

        ExternalResourceDescriptor res = result.get(0);

        assertEquals("mysql", res.getVendor());
        assertEquals("adk", res.getLabel());
        assertEquals("chef.metadyne.com", res.getHost());
        assertEquals(3000, res.getPort());
        assertEquals("mysql:1:" + MD5NameGenerator.getName(toHash), res.getName());
        assertEquals(Boolean.FALSE, Boolean.valueOf(res.isIncoming()));
    }

    @Test
    public void test_ExtractMeaningfulNames_ParserReturnsTwo() throws Exception {
        Frame frame = mock(Frame.class);
        String testUri = // invalid port for the first address
                "jdbc:oracle:thin:@(DESCRIPTION = (ADDRESS = (PROTOCOL = TCP)(HOST = 10.17.184.138)(PORT = boat)) " +
                        "(ADDRESS = (PROTOCOL = TCP)(HOST = 10.17.184.139)(PORT = 1521)))";
        List<ExternalResourceDescriptor> result = dbAnalyzer.extractMeaningfulNames(frame, testUri);

        assertEquals(2, result.size());

        ExternalResourceDescriptor res = result.get(0);
        String tohash1 = "oracle" + "" + "10.17.184.138" + -1;

        assertEquals("oracle", res.getVendor());
        assertEquals(null, res.getLabel());
        assertEquals("10.17.184.138", res.getHost());
        assertEquals(-1, res.getPort());
        assertEquals("oracle:1:" + MD5NameGenerator.getName(tohash1), res.getName());
        assertEquals(Boolean.FALSE, Boolean.valueOf(res.isIncoming()));

        res = result.get(1);
        String tohash2 = "oracle" + "" + "10.17.184.139" + 1521;
        assertEquals("oracle", res.getVendor());
        assertEquals(null, res.getLabel());
        assertEquals("10.17.184.139", res.getHost());
        assertEquals(1521, res.getPort());
        assertEquals("oracle:2:" + MD5NameGenerator.getName(tohash2), res.getName());
        assertEquals(Boolean.FALSE, Boolean.valueOf(res.isIncoming()));
    }

    @Test
    public void test_ExtractMeaningfulNames_NoParserRecognized() throws Exception {
        Frame frame = mock(Frame.class);
        String testUri = "jdbc:mydb:scheme://server-address:8080";
        List<ExternalResourceDescriptor> result = dbAnalyzer.extractMeaningfulNames(frame, testUri);

        String tohash = "mydb" + "" + "server-address" + 8080;
        assertEquals(1, result.size());

        ExternalResourceDescriptor res = result.get(0);

        assertEquals("mydb", res.getVendor());
        assertEquals("", res.getLabel());
        assertEquals("server-address", res.getHost());
        assertEquals(8080, res.getPort());
        assertEquals("mydb:1:" + MD5NameGenerator.getName(tohash), res.getName());
        assertEquals(Boolean.FALSE, Boolean.valueOf(res.isIncoming()));
    }
}
