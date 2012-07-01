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
package com.springsource.insight.plugin.files.tracker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.Test;

import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.SimpleFrameBuilder;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;


public class FilesTrackerExternalResourceAnalyzerTest {
	private final FilesTrackerExternalResourceAnalyzer analyzer = new FilesTrackerExternalResourceAnalyzer();
	
	@Test
	public void testLocateExternalResourceName() {
        final String PATH="/dummy/path/123";
		Trace trace = createValidTrace(PATH);

		List<ExternalResourceDescriptor> externalResourceDescriptors = (List<ExternalResourceDescriptor>) analyzer.locateExternalResourceName(trace);
		assertNotNull("No descriptors extracted", externalResourceDescriptors);
		assertEquals("Mismatched number of descriptors", 1, externalResourceDescriptors.size());        

		ExternalResourceDescriptor descriptor = externalResourceDescriptors.get(0);
		assertSame("Mismatched descriptor frame", trace.getRootFrame(), descriptor.getFrame());
		assertDescriptorContents("testLocateExternalResourceName", PATH, descriptor);
	}
	
	private static ExternalResourceDescriptor assertDescriptorContents (String testName, String path, ExternalResourceDescriptor descriptor) {

        assertEquals(testName + ": Mismatched label", path, descriptor.getLabel());
        assertEquals(testName + ": Mismatched type", ExternalResourceType.FILESTORE.name(), descriptor.getType());

        String expectedHash = MD5NameGenerator.getName(path);
        assertEquals(testName + ": Mismatched name", FilesTrackerDefinitions.TYPE.getName() + ":" + expectedHash, descriptor.getName());
        assertEquals(testName + ": Mismatched direction", Boolean.FALSE, Boolean.valueOf(descriptor.isIncoming()));
        return descriptor;
    }
	
	private Trace createValidTrace(String param) {
        SimpleFrameBuilder builder = new SimpleFrameBuilder();
        Operation op = createOperation(param);
        
        builder.enter(op);
        
        Frame frame = builder.exit();
        return Trace.newInstance(ApplicationName.valueOf("app"), TraceId.valueOf("0"), frame);
    }
	
	private Operation createOperation(String param) {
		Operation op = new Operation().type(FilesTrackerDefinitions.TYPE);
        
        op.put(FilesTrackerDefinitions.PATH_ATTR, param);
		return op;
	}
}
