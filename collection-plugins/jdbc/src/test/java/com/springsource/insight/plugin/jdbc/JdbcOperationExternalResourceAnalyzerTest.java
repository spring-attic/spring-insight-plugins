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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameId;
import com.springsource.insight.intercept.trace.SimpleFrame;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.time.TimeRange;

public class JdbcOperationExternalResourceAnalyzerTest extends Assert {
	@Test
	public void testCreateAndAddQueryExternalResourceDescriptorsEmptyDbDescriptors() {
		Collection<ExternalResourceDescriptor> result = 
				JdbcOperationExternalResourceAnalyzer.createAndAddQueryExternalResourceDescriptors(Collections.<ExternalResourceDescriptor>emptyList());
		
		assertEquals("Total number of descriptors with empty db descriptors", 0, ListUtil.size(result));
	}
	
	@Test
	public void testCreateAndAddQueryExternalResourceDescriptorsNullDbDescriptors() {
		Collection<ExternalResourceDescriptor> result = 
				JdbcOperationExternalResourceAnalyzer.createAndAddQueryExternalResourceDescriptors(null);
		
		assertEquals("Total number of descriptors with null db descriptors", 0, ListUtil.size(result));
	}
	
	@Test
	public void testCreateAndAddQueryExternalResourceDescriptorsNoSQL() {
		Collection<ExternalResourceDescriptor> dbDescriptors = createDbDescriptors(2, false);
		
		Collection<ExternalResourceDescriptor> result = 
				JdbcOperationExternalResourceAnalyzer.createAndAddQueryExternalResourceDescriptors(dbDescriptors);
		
		assertEquals("Total number of descriptors", dbDescriptors.size(), result.size());
		assertArrayEquals("descriptors content should remain the same", dbDescriptors.toArray(new ExternalResourceDescriptor[0]), 
																   result.toArray(new ExternalResourceDescriptor[0]));
		
	}
	
	@Test
	public void testCreateAndAddQueryExternalResourceDescriptorsWithSQL() {
		Collection<ExternalResourceDescriptor> dbDescriptors = createDbDescriptors(2, true);
		
		Collection<ExternalResourceDescriptor> result = 
				JdbcOperationExternalResourceAnalyzer.createAndAddQueryExternalResourceDescriptors(dbDescriptors);
		
		assertEquals("Total number of descriptors", 2*dbDescriptors.size(), result.size());
		
		int totalDbs = 0;
		int totalQuerys = 0;
		
		for(ExternalResourceDescriptor desc : result) {
			ExternalResourceType type = ExternalResourceType.valueOf(desc.getType());
			
			switch(type) {
				case DATABASE:
					totalDbs ++;
					break;
				case QUERY:
					assertQueryDescriptor(desc);
					totalQuerys ++;
					break;
				default:
					assertNotSame("Unexpected external resource type (should be QUERY or DATABASE)", 
							ExternalResourceType.OTHER, 
							type);
			}
		}
		
		assertEquals("Total db external resource descriptors", 2, totalDbs);
		assertEquals("Total query external resource descriptors", 2, totalQuerys);
	}
	
	private void assertQueryDescriptor(ExternalResourceDescriptor desc) {
		ExternalResourceDescriptor parent = desc.getParentDescriptor();
		assertNotNull("QUERY ExternalResourceDescriptor parent", parent);
		
		ExternalResourceType type = ExternalResourceType.valueOf(parent.getType());
		assertSame("QUERY ExternalResourceDescriptor parent type", ExternalResourceType.DATABASE, type);
		
		List<ExternalResourceDescriptor> children = parent.getChildren();
		assertNotNull("QUERY ExternalResourceDescriptor parent children", children);
		assertEquals("QUERY ExternalResourceDescriptor parent children size", 1, children.size());
		
		ExternalResourceDescriptor firstChild = children.get(0);
		assertNotNull("QUERY ExternalResourceDescriptor parent first child", firstChild);
		assertSame("QUERY ExternalResourceDescriptor parent first child", desc, firstChild);
		
		Frame frame = desc.getFrame();
		assertNotNull("QUERY ExternalResourceDescriptor frame", frame);
		
		Operation op = frame.getOperation();
		assertNotNull("QUERY ExternalResourceDescriptor frame operation", frame);
		
		String sql = op.get("sql", String.class);
		assertEquals("QUERY ExternalResourceDescriptor label", sql, desc.getLabel());
		
		assertEquals("QUERY ExternalResourceDescriptor vendor", parent.getVendor(), desc.getVendor());
		assertEquals("QUERY ExternalResourceDescriptor host", parent.getHost(), desc.getHost());
		assertEquals("QUERY ExternalResourceDescriptor port", parent.getPort(), desc.getPort());
		assertEquals("QUERY ExternalResourceDescriptor incoming", parent.isIncoming(), desc.isIncoming());
	}
	
	private Collection<ExternalResourceDescriptor> createDbDescriptors(int num, boolean createQuery) {
		Collection<ExternalResourceDescriptor> descriptors = new ArrayList<ExternalResourceDescriptor>();
		
		for(int i=0; i<num; i++) {
			Operation op = new Operation();
			if (createQuery) {
				op.put("sql", "select * from table" + i);
			}
			Frame frame = new SimpleFrame(FrameId.valueOf(i), null, op, 
									TimeRange.milliTimeRange(0L, 1L), Collections.<Frame>emptyList());
			
			ExternalResourceDescriptor descriptor=new ExternalResourceDescriptor(frame, 
					 "name"+i,
					 "label"+i,
					 ExternalResourceType.DATABASE.name(),
					 "vendor"+i,
					 "vendor"+i,
					 i,
					 null, 
					 false);
			
			descriptors.add(descriptor);
		}
		
		return descriptors;
	}

}
