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

package com.springsource.insight.plugin.redis;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.server.ServerName;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameId;
import com.springsource.insight.intercept.trace.SimpleFrame;
import com.springsource.insight.intercept.trace.SimpleFrameBuilder;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;
import com.springsource.insight.util.time.TimeRange;

/**
 */
public class RedisExternalResourceAnalyzerTest extends Assert {
	public RedisExternalResourceAnalyzerTest () {
		super();
	}

	@Test
	public void testLocateDatabaseURI() throws Exception {
		Operation op = new Operation();
		op.type(RedisExternalResourceAnalyzer.TYPE);		
		op.put("host", "localhost");
		op.put("port", 6379);
		op.put("dbName", "dbName");
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

		List<ExternalResourceDescriptor> externalResourceDescriptors =
				(List<ExternalResourceDescriptor>) new RedisExternalResourceAnalyzer().locateExternalResourceName(trace);
		ExternalResourceDescriptor externalResourceDescriptor = externalResourceDescriptors.get(0);

		assertEquals(frame, externalResourceDescriptor.getFrame());
		assertEquals(ExternalResourceType.DATABASE.name(), externalResourceDescriptor.getType());
		assertEquals("redis:" + MD5NameGenerator.getName("dbNamelocalhost"+6379), externalResourceDescriptor.getName());
		assertEquals("Redis", externalResourceDescriptor.getVendor());
		assertEquals("dbName", externalResourceDescriptor.getLabel());
		assertEquals("localhost", externalResourceDescriptor.getHost());
		assertEquals(6379, externalResourceDescriptor.getPort());
		assertEquals(Boolean.FALSE, Boolean.valueOf(externalResourceDescriptor.isIncoming()));
	}

	@Test
	public void testExactlyTwoDifferentExternalResourceNames() {   	
		Operation op1 = new Operation();
		op1.type(RedisExternalResourceAnalyzer.TYPE);		
		op1.putAnyNonEmpty("host", "127.0.0.1");
		op1.put("port", 6379);
		op1.put("dbName", "dbName");
		
		Operation op2 = new Operation();
		op2.type(RedisExternalResourceAnalyzer.TYPE);	
		
		op2.put("port", 6379);
		op2.put("dbName", "dbName2");
		
		Operation dummyOp = new Operation();

		SimpleFrameBuilder builder = new SimpleFrameBuilder();
		builder.enter(new Operation().type(OperationType.HTTP));
		builder.enter(op2);
		builder.exit();
		builder.enter(dummyOp);
		builder.enter(op1);
		builder.exit();
		builder.exit();
		Frame frame = builder.exit();
		Trace trace = Trace.newInstance(ApplicationName.valueOf("app"), TraceId.valueOf("0"), frame);

		List<ExternalResourceDescriptor> externalResourceDescriptors =
				(List<ExternalResourceDescriptor>) new RedisExternalResourceAnalyzer().locateExternalResourceName(trace);

		assertEquals(2, externalResourceDescriptors.size());        

		ExternalResourceDescriptor descriptor = externalResourceDescriptors.get(0);        
		assertEquals(op2, descriptor.getFrame().getOperation());
		assertEquals("dbName2", descriptor.getLabel());
		assertEquals(ExternalResourceType.DATABASE.name(), descriptor.getType());
		assertEquals("Redis", descriptor.getVendor());
		assertEquals(null, descriptor.getHost());
		assertEquals(6379, descriptor.getPort());
		String expectedHash = MD5NameGenerator.getName("dbName2"+null+6379);
		assertEquals("redis:" + expectedHash, descriptor.getName());
		assertEquals(Boolean.FALSE, Boolean.valueOf(descriptor.isIncoming()));

		descriptor = externalResourceDescriptors.get(1);        
		assertEquals(op1, descriptor.getFrame().getOperation());
		assertEquals("dbName", descriptor.getLabel());
		assertEquals(ExternalResourceType.DATABASE.name(), descriptor.getType());
		assertEquals("Redis", descriptor.getVendor());
		assertEquals("127.0.0.1", descriptor.getHost());
		assertEquals(6379, descriptor.getPort());
		expectedHash = MD5NameGenerator.getName("dbName127.0.0.1"+6379);
		assertEquals("redis:" + expectedHash, descriptor.getName());
		assertEquals(Boolean.FALSE, Boolean.valueOf(descriptor.isIncoming()));
	}
}
