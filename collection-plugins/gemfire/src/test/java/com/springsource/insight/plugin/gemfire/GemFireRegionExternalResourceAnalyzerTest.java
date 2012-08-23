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


package com.springsource.insight.plugin.gemfire;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.springsource.insight.collection.test.AbstractCollectionTestSupport;
import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
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

public class GemFireRegionExternalResourceAnalyzerTest extends AbstractCollectionTestSupport  {

	GemFireRegionExternalResourceAnalyzer analyzer;
	
	@Before
	@Override
	public void setUp() {
		super.setUp();
		analyzer = new GemFireRegionExternalResourceAnalyzer();
	}

	@Test
	public void testLocateGemFireURI() throws Exception {
		Operation op = new Operation();		
		op.type(GemFireDefenitions.TYPE_REGION.getType());
		
		String fieldPath = "/kuku";
		op.put(GemFireDefenitions.FIELD_PATH, fieldPath);
		
		OperationList opList = op.createList(GemFireDefenitions.FIELD_SERVERS);		
		String uri = "huh:8080";
		opList.add(uri);
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
		Collection<ExternalResourceDescriptor>    res=analyzer.locateExternalResourceName(trace);
		assertEquals("Mismatched number of results", 1, res.size());
		ExternalResourceDescriptor externalResourceDescriptor = res.iterator().next();

		assertEquals(frame, externalResourceDescriptor.getFrame());
		assertEquals(ExternalResourceType.KVSTORE.name(), externalResourceDescriptor.getType());
		assertEquals(GemFireDefenitions.GEMFIRE + ":" + MD5NameGenerator.getName(uri+fieldPath), externalResourceDescriptor.getName());
		assertEquals(GemFireDefenitions.GEMFIRE, externalResourceDescriptor.getVendor());
		assertEquals(fieldPath, externalResourceDescriptor.getLabel());
		assertEquals(Boolean.FALSE, Boolean.valueOf(externalResourceDescriptor.isIncoming()));
		
	}

	@Test
	public void testLocateGemFireURI_noRegion() throws Exception {
		Operation op = new Operation();		
		op.type(GemFireDefenitions.TYPE_REGION.getType());
		
		OperationList opList = op.createList(GemFireDefenitions.FIELD_SERVERS);		
		String uri = "huh:8080";
		opList.add(uri);
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
        Collection<ExternalResourceDescriptor>    res=analyzer.locateExternalResourceName(trace);
        assertEquals("Mismatched number of results", 1, res.size());
        ExternalResourceDescriptor externalResourceDescriptor = res.iterator().next();

		assertEquals(frame, externalResourceDescriptor.getFrame());
		assertEquals(ExternalResourceType.KVSTORE.name(), externalResourceDescriptor.getType());
		assertEquals(GemFireDefenitions.GEMFIRE + ":" + MD5NameGenerator.getName(uri+null), externalResourceDescriptor.getName());
		assertEquals(GemFireDefenitions.GEMFIRE, externalResourceDescriptor.getVendor());
		assertEquals(null, externalResourceDescriptor.getLabel());		
		assertEquals(Boolean.FALSE, Boolean.valueOf(externalResourceDescriptor.isIncoming()));
	}
	
}
