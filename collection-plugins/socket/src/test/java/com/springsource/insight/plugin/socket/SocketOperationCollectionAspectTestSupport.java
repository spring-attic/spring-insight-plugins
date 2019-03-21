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
package com.springsource.insight.plugin.socket;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;

import com.springsource.insight.collection.ObscuredValueSetMarker;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.collection.OperationListCollector;
import com.springsource.insight.collection.http.HttpObfuscator;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.plugin.CollectionSettingName;
import com.springsource.insight.intercept.server.ServerName;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameId;
import com.springsource.insight.intercept.trace.SimpleFrame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.time.TimeRange;

/**
 *
 */
public abstract class SocketOperationCollectionAspectTestSupport
        extends OperationCollectionAspectTestSupport {
    protected static final int TEST_PORT = 7365;
    protected static final String TEST_HOST = "localhost";
    private SocketCollectOperationContext originalContext;
    private final ObscuredValueSetMarker marker = new ObscuredValueSetMarker();

    protected SocketOperationCollectionAspectTestSupport() {
        super();
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();

        SocketOperationCollectionAspectSupport aspectInstance = (SocketOperationCollectionAspectSupport) getAspect();
        originalContext = aspectInstance.getSocketCollectOperationContext();
        marker.clear();
        aspectInstance.setSocketCollectOperationContext(new SocketCollectOperationContext(new HttpObfuscator(marker)));
    }

    @After
    @Override
    public void restore() {
        SocketOperationCollectionAspectSupport aspectInstance = (SocketOperationCollectionAspectSupport) getAspect();
        aspectInstance.setSocketCollectOperationContext(originalContext);
        marker.clear();    // make sure again
    }

    @Override
    protected OperationCollector createSpiedOperationCollector(OperationCollector originalCollector) {
        return new OperationListCollector();
    }

    @Override
    protected Operation getLastEnteredOperation(OperationCollector spiedCollector) {
        OperationListCollector collector = (OperationListCollector) spiedCollector;
        List<Operation> ops = collector.getCollectedOperations();
        if (ListUtil.size(ops) > 0) {
            return ops.get(ops.size() - 1);
        } else {
            return null;
        }
    }

    protected Operation assertSocketOperation(String action, String addr, int port) {
        Operation op = getLastEntered();
        assertNotNull("No operation extracted", op);
        assertEquals("Mismatched operation type", SocketDefinitions.TYPE, op.getType());
        assertEquals("Mismatched action", action, op.get(SocketDefinitions.ACTION_ATTR, String.class));
        assertEquals("Mismatched address", addr, op.get(SocketDefinitions.ADDRESS_ATTR, String.class));
        assertEquals("Mismatched port", port, op.getInt(SocketDefinitions.PORT_ATTR, (-1)));
        return op;
    }

    protected Operation runExternalResourceAnalyzer(Operation op, ExternalResourceType expType, String expAddress, int expPort) {
        Frame frame = new SimpleFrame(FrameId.valueOf("1"), null, op,
                TimeRange.milliTimeRange(0, 1L),
                Collections.<Frame>emptyList());
        Trace trace = new Trace(ServerName.valueOf("fake-server"),
                ApplicationName.valueOf("fake-app"),
                new Date(System.currentTimeMillis()),
                TraceId.valueOf("fake-id"),
                frame);
        SocketExternalResourceAnalyzer analyzer = SocketExternalResourceAnalyzer.getInstance();
        Collection<ExternalResourceDescriptor> results = analyzer.locateExternalResourceName(trace);
        assertEquals("Mismatched number of results: " + results, 1, ListUtil.size(results));

        ExternalResourceDescriptor desc = ListUtil.getFirstMember(results);
        String expectedName = MD5NameGenerator.getName(expAddress + ":" + expPort);
        assertSame("Mismatched frame", frame, desc.getFrame());
        assertEquals("Mismatched name", expectedName, desc.getName());
        assertEquals("Mismatched type", expType.name(), desc.getType());
        assertNull("Unexpected vendor", desc.getVendor());
        assertEquals("Mismatched host", expAddress, desc.getHost());
        assertEquals("Mismatched port", TEST_PORT, desc.getPort());
        assertEquals("Mismatched direction", Boolean.FALSE, Boolean.valueOf(desc.isIncoming()));

        return op;
    }

    protected ObscuredValueSetMarker setupObscuredTest(CollectionSettingName settingName, String pattern) {
        SocketOperationCollectionAspectSupport aspectInstance =
                (SocketOperationCollectionAspectSupport) getAspect();
        SocketCollectOperationContext context = aspectInstance.getSocketCollectOperationContext();
        context.incrementalUpdate(settingName, pattern);
        return (ObscuredValueSetMarker) context.getObscuredValueMarker();
    }

    protected void assertObscureTestResults(ObscuredValueSetMarker markedValues,
                                            String pattern,
                                            String value,
                                            boolean shouldObscure) {
        if (shouldObscure) {
            assertTrue("assertObscureTestResults(" + pattern + ") value not obscured: " + value,
                    markedValues.contains(value));
        } else {
            assertFalse("assertObscureTestResults(" + pattern + ") value un-necessarily obscured",
                    markedValues.contains(value));
        }
    }
}
