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
package com.springsource.insight.plugin.socket;

import static java.util.Collections.unmodifiableSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;

import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.plugin.CollectionSettingName;
import com.springsource.insight.intercept.plugin.CollectionSettingsRegistry;
import com.springsource.insight.intercept.server.ServerName;
import com.springsource.insight.intercept.topology.ExternalResourceAnalyzer;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameId;
import com.springsource.insight.intercept.trace.ObscuredValueMarker;
import com.springsource.insight.intercept.trace.SimpleFrame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;
import com.springsource.insight.util.time.TimeRange;

/**
 * 
 */
public abstract class SocketOperationCollectionAspectTestSupport
            extends OperationCollectionAspectTestSupport {
    protected static final int    TEST_PORT=7365;
    protected static final String TEST_HOST="localhost";

    protected SocketOperationCollectionAspectTestSupport() {
        super();
    }
    
    protected Operation assertSocketOperation (String action, String addr, int port) {
        Operation   op=getLastEntered();
        Assert.assertNotNull("No operation extracted", op);
        Assert.assertEquals("Mismatched operation type", SocketDefinitions.TYPE, op.getType());
        Assert.assertEquals("Mismatched action", action, op.get(SocketDefinitions.ACTION_ATTR, String.class));
        Assert.assertEquals("Mismatched address", addr, op.get(SocketDefinitions.ADDRESS_ATTR, String.class));
        Assert.assertEquals("Mismatched port", Integer.valueOf(port), op.get(SocketDefinitions.PORT_ATTR, Integer.class));
        return op;
    }

    protected Operation runExternalResourceAnalyzer (Operation op, ExternalResourceType expType, String expAddress, int expPort) {
        Frame       frame=new SimpleFrame(FrameId.valueOf("1"), null, op,
                                          TimeRange.milliTimeRange(0, 1),
                                          Collections.<Frame>emptyList());
        Trace       trace=new Trace(ServerName.valueOf("fake-server"),
                                    ApplicationName.valueOf("fake-app"),
                                    new Date(System.currentTimeMillis()),
                                    TraceId.valueOf("fake-id"),
                                    frame);
        ExternalResourceAnalyzer                analyzer=new SocketExternalResourceAnalyzer();
        Collection<ExternalResourceDescriptor>  results=analyzer.locateExternalResourceName(trace);
        Assert.assertFalse("No results returned", (results == null) || results.isEmpty());
        Assert.assertEquals("Too many results returned", 1, results.size());

        ExternalResourceDescriptor  desc=results.iterator().next();
        String                      expectedName=MD5NameGenerator.getName(expAddress + ":" + expPort);
        Assert.assertSame("Mismatched frame", frame, desc.getFrame());
        Assert.assertEquals("Mismatched name", expectedName, desc.getName());
        Assert.assertEquals("Mismatched type", expType.name(), desc.getType());
        Assert.assertNull("Unexpected vendor", desc.getVendor());
        Assert.assertEquals("Mismatched host", expAddress, desc.getHost());
        Assert.assertEquals("Mismatched port", TEST_PORT, desc.getPort());
        Assert.assertEquals("Mismatched direction", Boolean.FALSE, Boolean.valueOf(desc.isIncoming()));
        
        return op;
    }

    protected DummyObscuredValueMarker setupObscuredTest (
            CollectionSettingName settingName, String pattern) {
        SocketOperationCollectionAspectSupport  aspectInstance=
                (SocketOperationCollectionAspectSupport) getAspect();
        SocketCollectOperationContext           context=aspectInstance.getSocketCollectOperationContext();
        DummyObscuredValueMarker                marker=new DummyObscuredValueMarker();
        context.setObscuredValueMarker(marker);
        
        CollectionSettingsRegistry  registry=CollectionSettingsRegistry.getInstance();
        registry.set(settingName, pattern);
        return marker;
    }

    protected void assertObscureTestResults (DummyObscuredValueMarker   marker,
                                             String                     pattern,
                                             String                     testAddress,
                                             boolean                    shouldObscure) {
        Set<?>  markedValues=marker.getValues();
        if (shouldObscure) {
            Assert.assertTrue("assertObscureTestResults(" + pattern + ") Address not obscured: " + testAddress,
                              markedValues.contains(testAddress));
        } else {
            Assert.assertFalse("assertObscureTestResults(" + pattern + ") Address un-necessarily obscured",
                               markedValues.contains(testAddress));
        }
    }

    public static class DummyObscuredValueMarker implements ObscuredValueMarker {
        private Set<Object> objects = new HashSet<Object>();

        public Set<Object> getValues() {
            return unmodifiableSet(objects);
        }

        public void markObscured(Object o) {
            objects.add(o);
        }
    }
}
