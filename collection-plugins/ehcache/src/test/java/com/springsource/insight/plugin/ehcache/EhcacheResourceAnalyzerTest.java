/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.insight.plugin.ehcache;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.springsource.insight.collection.test.AbstractCollectionTestSupport;
import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.SimpleFrameBuilder;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;
import com.springsource.insight.util.ListUtil;

public class EhcacheResourceAnalyzerTest extends AbstractCollectionTestSupport {
    private final EhcacheExternalResourceAnalyzer analyzer = EhcacheExternalResourceAnalyzer.getInstance();

    public EhcacheResourceAnalyzerTest() {
        super();
    }

    @Test
    public void testLocateExternalResourceName() {
        final String NAME = "testLocateExternalResourceName";
        Trace trace = createValidTrace(NAME);

        Collection<ExternalResourceDescriptor> externalResourceDescriptors = analyzer.locateExternalResourceName(trace);
        assertNotNull("No descriptors extracted", externalResourceDescriptors);
        assertEquals("Mismatched number of descriptors", 1, externalResourceDescriptors.size());

        ExternalResourceDescriptor descriptor = ListUtil.getFirstMember(externalResourceDescriptors);
        assertSame("Mismatched descriptor frame", trace.getRootFrame(), descriptor.getFrame());
        assertDescriptorContents("testLocateExternalResourceName", NAME, descriptor);
    }

    @Test
    public void testExactlyTwoDifferentExternalResourceNames() {
        final String NAME1 = "testCache1", NAME2 = "testCache2";
        Operation op1 = createOperation(NAME1), op2 = createOperation(NAME2);

        SimpleFrameBuilder builder = new SimpleFrameBuilder();
        builder.enter(new Operation().type(OperationType.HTTP));
        builder.enter(op2);
        builder.exit();
        builder.enter(new Operation().type(OperationType.METHOD));
        builder.enter(op1);
        builder.exit();
        builder.exit();
        Frame frame = builder.exit();
        Trace trace = Trace.newInstance(ApplicationName.valueOf("app"), TraceId.valueOf("0"), frame);

        List<ExternalResourceDescriptor> externalResourceDescriptors =
                (List<ExternalResourceDescriptor>) analyzer.locateExternalResourceName(trace);
        assertNotNull("No descriptors extracted", externalResourceDescriptors);
        assertEquals("Mismatched number of descriptors", 2, externalResourceDescriptors.size());

        ExternalResourceDescriptor descriptor = externalResourceDescriptors.get(0);
        assertSame("Mismatched 2nd operation instance", op2, descriptor.getFrame().getOperation());
        assertDescriptorContents("testExactlyTwoDifferentExternalResourceNames", NAME2, descriptor);

        descriptor = externalResourceDescriptors.get(1);
        assertSame("Mismatched 1st operation instance", op1, descriptor.getFrame().getOperation());
        assertDescriptorContents("testExactlyTwoDifferentExternalResourceNames", NAME1, descriptor);
    }

    private static ExternalResourceDescriptor assertDescriptorContents(
            String testName, String name, ExternalResourceDescriptor descriptor) {

        assertEquals(testName + ": Mismatched label", name, descriptor.getLabel());
        assertEquals(testName + ": Mismatched type", ExternalResourceType.CACHE.name(), descriptor.getType());
        assertEquals(testName + ": Mismatched vendor", "ehcache", descriptor.getVendor());
        assertEquals(testName + ": Mismatched host", null, descriptor.getHost());
        assertEquals(testName + ": Mismatched port", -1, descriptor.getPort());
        assertEquals(testName + ": Mismatched direction", Boolean.FALSE, Boolean.valueOf(descriptor.isIncoming()));

        String expectedHash = MD5NameGenerator.getName(name);
        assertEquals(testName + ": Mismatched name", "ehcache:" + expectedHash, descriptor.getName());
        return descriptor;
    }

    private Trace createValidTrace(String name) {
        SimpleFrameBuilder builder = new SimpleFrameBuilder();
        Operation op = createOperation(name);

        builder.enter(op);

        Frame frame = builder.exit();
        return Trace.newInstance(ApplicationName.valueOf("app"), TraceId.valueOf("0"), frame);
    }

    private Operation createOperation(String name) {
        Operation op = new Operation().type(EhcacheDefinitions.CACHE_OPERATION);

        op.put(EhcacheDefinitions.NAME_ATTRIBUTE, name);
        return op;
    }

}
