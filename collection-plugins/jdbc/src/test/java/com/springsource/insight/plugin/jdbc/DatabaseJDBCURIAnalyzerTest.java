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
package com.springsource.insight.plugin.jdbc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.server.ServerName;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameId;
import com.springsource.insight.intercept.trace.SimpleFrame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;
import com.springsource.insight.util.time.TimeRange;

/**
 */
public class DatabaseJDBCURIAnalyzerTest {
	private DatabaseJDBCURIAnalyzer dbAnalyzer;
	
	public DatabaseJDBCURIAnalyzerTest() {
        super();
    }

    @Before
	public void setup() {
		dbAnalyzer = new TestJDBCURIAnalyzer();
	}

	@Test
	public void testLocateDatabaseURI() throws Exception {
		Operation op = TestJDBCURIAnalyzer.createOperation();
		String jdbcUri = "jdbc:foobar://huh:8080";
		op.put(OperationFields.CONNECTION_URL, jdbcUri);
		Frame frame = new SimpleFrame(FrameId.valueOf("0"),
				null,
				op,
				TimeRange.milliTimeRange(0, 1),
				Collections.<Frame>emptyList());

		Trace trace = new Trace(ServerName.valueOf("fake-server"),
				ApplicationName.valueOf("fake-app"),
				new Date(),
				TraceId.valueOf("fake-id"),
				frame);

		ExternalResourceDescriptor externalResourceDescriptor = dbAnalyzer.locateExternalResourceName(trace).get(0);

		assertEquals(frame, externalResourceDescriptor.getFrame());
		assertEquals(ExternalResourceType.DATABASE.name(), externalResourceDescriptor.getType());
		assertEquals("foobar:1:" + MD5NameGenerator.getName(jdbcUri), externalResourceDescriptor.getName());
		assertEquals(Boolean.FALSE, Boolean.valueOf(externalResourceDescriptor.isIncoming()));
	}

	@Test
	public void test_ExtractMeaningfulNames_ParserReturnsOne() throws Exception {
		Frame frame = mock(Frame.class);
		String testUri = "jdbc:mysql://chef.metadyne.com:3000/adk?user=username&password=test";
		List<ExternalResourceDescriptor> result = dbAnalyzer.extractMeaningfulNames(frame, testUri);

		assertEquals(1, result.size());
		
		ExternalResourceDescriptor res = result.get(0);
		
		assertEquals("mysql", res.getVendor());
		assertEquals("adk", res.getLabel());
		assertEquals("chef.metadyne.com", res.getHost());
		assertEquals(3000, res.getPort());
		assertEquals("mysql:1:" + MD5NameGenerator.getName(testUri), res.getName());
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
		
		assertEquals("oracle", res.getVendor());
		assertEquals(null, res.getLabel());
		assertEquals("10.17.184.138", res.getHost());
		assertEquals(-1, res.getPort());
		assertEquals("oracle:1:" + MD5NameGenerator.getName(testUri), res.getName());
		assertEquals(Boolean.FALSE, Boolean.valueOf(res.isIncoming()));
		
		res = result.get(1);
		
		assertEquals("oracle", res.getVendor());
		assertEquals(null, res.getLabel());
		assertEquals("10.17.184.139", res.getHost());
		assertEquals(1521, res.getPort());
		assertEquals("oracle:2:" + MD5NameGenerator.getName(testUri), res.getName());
		assertEquals(Boolean.FALSE, Boolean.valueOf(res.isIncoming()));
	}
	
	@Test
	public void test_ExtractMeaningfulNames_NoParserRecognized() throws Exception {
		Frame frame = mock(Frame.class);
		String testUri = "jdbc:mydb:scheme://server-address:8080";
		List<ExternalResourceDescriptor> result = dbAnalyzer.extractMeaningfulNames(frame, testUri);
		
		assertEquals(1, result.size());

		ExternalResourceDescriptor res = result.get(0);
		
		assertEquals("mydb", res.getVendor());
		assertEquals("", res.getLabel());
		assertEquals("server-address", res.getHost());
		assertEquals(8080, res.getPort());
		assertEquals("mydb:1:" + MD5NameGenerator.getName(testUri), res.getName());
		assertEquals(Boolean.FALSE, Boolean.valueOf(res.isIncoming()));
	}
	
	static class TestJDBCURIAnalyzer extends DatabaseJDBCURIAnalyzer {
	    static final OperationType TYPE=OperationType.valueOf("analyzer-test");
	    TestJDBCURIAnalyzer () {
	        super(TYPE);
	    }

	    static Operation createOperation () {
	        return createOperation(new Operation());
	    }

	    static Operation createOperation (Operation op) {
	        return op.type(TYPE);
	    }
	}
}
