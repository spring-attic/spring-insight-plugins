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

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.plugin.CollectionSettingsRegistry;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.ListUtil;

public class JdbcOperationExternalResourceAnalyzerTest extends AbstractDatabaseJDBCURIAnalyzerTest {
	
	private CollectionSettingsRegistry registry;
	private JdbcOperationExternalResourceAnalyzer analyzer;
	
	@Before
	@Override
	public void setUp() {
		super.setUp();
		registry = new CollectionSettingsRegistry();
		analyzer = new JdbcOperationExternalResourceAnalyzer(registry);
	}
	
	@Test
	public void testIncrementalUpdate() {
		assertInitialState();
		
		//update active state
		registry.set(JdbcOperationExternalResourceAnalyzer.CS_NAME, Boolean.TRUE);
		
		boolean active = analyzer.isGeneratingExternalResources();
		assertTrue("Active state should be true after update", active);
		
		//add application to ignore list
		ApplicationName appName = ApplicationName.valueOf("test-app");
		registry.set(JdbcOperationExternalResourceAnalyzer.createApplicationCollectionSettingName(appName), Boolean.FALSE);
		
		Collection<ApplicationName> apps = analyzer.getDisabledApplicationNames();
		assertEquals("Total disabled apps", 1, apps.size());
		assertTrue(appName + "is known when disabled", analyzer.isApplicationNameKnown(appName));
		
		ApplicationName disabledAppName = ListUtil.getFirstMember(apps);
		assertEquals("The only disabled app", appName, disabledAppName);
		
		//removed application from ignore list
		registry.set(JdbcOperationExternalResourceAnalyzer.createApplicationCollectionSettingName(appName), Boolean.TRUE);
		
		apps = analyzer.getDisabledApplicationNames();
		assertTrue("Total disabled apps is empty", apps.isEmpty());
		assertTrue(appName + "is known when enabled", analyzer.isApplicationNameKnown(appName));
	}
	
	@Test
	public void testLocateExternalResourceNameWhenDisabled() {
		assertInitialState();
		
		Operation op = createJdbcOperation("jdbc:foobar://huh:8080");
		op.type(JdbcOperationExternalResourceAnalyzer.TYPE);
		
		Frame frame = createJdbcFrame(op);
		Trace trace = createJdbcTrace(frame);
		
		Collection<ExternalResourceDescriptor> resources = analyzer.locateExternalResourceName(trace);
		
		//make sure that no query external resources were created
		assertNotNull("external resource descriptors list", resources);
		assertEquals("total external resource descriptors", 1, resources.size());
		
		ExternalResourceDescriptor firstDescriptor = ListUtil.getFirstMember(resources);
		ExternalResourceType type = ExternalResourceType.valueOf(firstDescriptor.getType());
		assertSame("first descriptor type", ExternalResourceType.DATABASE, type);
		assertFalse("first descriptor is a parent", firstDescriptor.isParent());
		
		assertTrue(trace.getAppName() + "is known", analyzer.isApplicationNameKnown(trace.getAppName()));
	}
	
	@Test
	public void testLocateExternalResourceNameWhenEnabledAndWithoutSql() {
		assertInitialState();
		
		//enable query external resources creation
		registry.set(JdbcOperationExternalResourceAnalyzer.CS_NAME, Boolean.TRUE);
		
		Operation op = createJdbcOperation("jdbc:foobar://huh:8080");
		op.type(JdbcOperationExternalResourceAnalyzer.TYPE);
		
		Frame frame = createJdbcFrame(op);
		Trace trace = createJdbcTrace(frame);
		
		Collection<ExternalResourceDescriptor> resources = analyzer.locateExternalResourceName(trace);
		
		//make sure that no query external resources were created
		assertNotNull("external resource descriptors list", resources);
		assertEquals("total external resource descriptors", 1, resources.size());
		
		ExternalResourceDescriptor firstDescriptor = ListUtil.getFirstMember(resources);
		ExternalResourceType type = ExternalResourceType.valueOf(firstDescriptor.getType());
		assertSame("first descriptor type", ExternalResourceType.DATABASE, type);
		assertFalse("first descriptor is a parent", firstDescriptor.isParent());
		
		assertTrue(trace.getAppName() + "is known", analyzer.isApplicationNameKnown(trace.getAppName()));
	}
	
	@Test
	public void testLocateExternalResourceNameWhenEnabledAndWithSql() {
		assertInitialState();
		
		//enable query external resources creation
		registry.set(JdbcOperationExternalResourceAnalyzer.CS_NAME, Boolean.TRUE);
		
		Operation op = createJdbcOperation("jdbc:foobar://huh:8080");
		op.type(JdbcOperationExternalResourceAnalyzer.TYPE);
		op.put("sql", "selct * from all");
		
		Frame frame = createJdbcFrame(op);
		Trace trace = createJdbcTrace(frame);
		
		Collection<ExternalResourceDescriptor> resources = analyzer.locateExternalResourceName(trace);
		
		//make sure that no query external resources were created
		assertNotNull("external resource descriptors list", resources);
		assertEquals("total external resource descriptors", 2, resources.size());
		
		assertTrue(trace.getAppName() + "is known", analyzer.isApplicationNameKnown(trace.getAppName()));
		
		int totalDbs = 0;
		int totalQuerys = 0;
		
		for(ExternalResourceDescriptor desc : resources) {
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
		
		assertEquals("Total db external resource descriptors", 1, totalDbs);
		assertEquals("Total query external resource descriptors", 1, totalQuerys);
	}
	
	@Test
	public void testLocateExternalResourceNameWhenEnabledWithSqlAndDisabledApp() {
		assertInitialState();
		
		//enable query external resources creation
		registry.set(JdbcOperationExternalResourceAnalyzer.CS_NAME, Boolean.TRUE);
		
		Operation op = createJdbcOperation("jdbc:foobar://huh:8080");
		op.type(JdbcOperationExternalResourceAnalyzer.TYPE);
		
		Frame frame = createJdbcFrame(op);
		Trace trace = createJdbcTrace(frame);
		

		registry.set(JdbcOperationExternalResourceAnalyzer.createApplicationCollectionSettingName(trace.getAppName()), Boolean.FALSE);
		
		Collection<ExternalResourceDescriptor> resources = analyzer.locateExternalResourceName(trace);
		
		//make sure that no query external resources were created
		assertNotNull("external resource descriptors list", resources);
		assertEquals("total external resource descriptors", 1, resources.size());
		
		ExternalResourceDescriptor firstDescriptor = ListUtil.getFirstMember(resources);
		ExternalResourceType type = ExternalResourceType.valueOf(firstDescriptor.getType());
		assertSame("first descriptor type", ExternalResourceType.DATABASE, type);
		assertFalse("first descriptor is a parent", firstDescriptor.isParent());
		
		assertTrue(trace.getAppName() + "is known", analyzer.isApplicationNameKnown(trace.getAppName()));
	}
	
	private void assertInitialState() {
		boolean active = analyzer.isGeneratingExternalResources();
		Collection<ApplicationName> apps = analyzer.getDisabledApplicationNames();
		
		assertFalse("Generating queries external resources should be disabled by default", active);
		
		assertNotNull("Disabled apps collection", apps);
		assertTrue("Disabled apps should be empty by default", apps.isEmpty());
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
}
