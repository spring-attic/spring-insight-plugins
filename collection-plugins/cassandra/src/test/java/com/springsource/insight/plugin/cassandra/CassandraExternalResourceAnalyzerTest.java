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
package com.springsource.insight.plugin.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.server.ServerName;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.trace.*;
import com.springsource.insight.util.time.TimeRange;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.*;

public class CassandraExternalResourceAnalyzerTest {

    @Test
    public void testLocateExternalResourceName() {

        String clusterName = "clustername";
        String keyspace = "keyspace";
        CassandraExternalResourceAnalyzer resourceAnalyzer = CassandraExternalResourceAnalyzer.getInstance();
        Operation op = createOperation(CassandraExternalResourceAnalyzer.TYPE, keyspace, clusterName, 1234, "127.0.0.1", "128.0.0.1");
        Frame frame = createFrame(op);
        Trace trace = createTrace(frame);
        Collection<ExternalResourceDescriptor> externalResourceDescriptors = resourceAnalyzer.locateExternalResourceName(trace, Collections.singletonList(frame));
        assertNotNull(externalResourceDescriptors);
        assertEquals("One external resource", 1, externalResourceDescriptors.size());
        ExternalResourceDescriptor descriptor = externalResourceDescriptors.iterator().next();
        assertNotNull(descriptor.getFrame());
        assertFalse(descriptor.isIncoming());
        assertEquals(descriptor.getType(), ExternalResourceType.MAPSTORE.toString());
        assertEquals("External resource name", CassandraExternalResourceAnalyzer.VENDOR + ":" + clusterName + ":" + keyspace, descriptor.getName());
        assertEquals("port", 1234, descriptor.getPort());
        assertEquals("host", clusterName, descriptor.getHost());
    }

    @Test
    public void testLocateExternalResourceNameNoClusterName() {

        String keyspace = "keyspace";
        CassandraExternalResourceAnalyzer resourceAnalyzer = CassandraExternalResourceAnalyzer.getInstance();
        Operation op = createOperation(CassandraExternalResourceAnalyzer.TYPE, keyspace, null, 1234, "127.0.0.1", "128.0.0.1");
        Frame frame = createFrame(op);
        Trace trace = createTrace(frame);
        Collection<ExternalResourceDescriptor> externalResourceDescriptors = resourceAnalyzer.locateExternalResourceName(trace, Collections.singletonList(frame));
        assertNotNull(externalResourceDescriptors);
        assertEquals("One external resource", 1, externalResourceDescriptors.size());
        ExternalResourceDescriptor descriptor = externalResourceDescriptors.iterator().next();
        assertNotNull(descriptor.getFrame());
        assertFalse(descriptor.isIncoming());
        assertEquals(descriptor.getType(), ExternalResourceType.MAPSTORE.toString());
        assertEquals("External resource name", CassandraExternalResourceAnalyzer.VENDOR + ":"
                + CassandraExternalResourceAnalyzer.DEFAULT_CLUSTER_NAME + ":" + keyspace , descriptor.getName());
        assertEquals("port", 1234, descriptor.getPort());
        assertEquals("host", CassandraExternalResourceAnalyzer.DEFAULT_CLUSTER_NAME, descriptor.getHost());
    }

    private Trace createTrace(Frame frame) {
        Trace trace = new Trace(ServerName.valueOf("fake-server"),
                ApplicationName.valueOf("fake-app"),
                new Date(),
                TraceId.valueOf("fake-id"),
                frame);
        return trace;
    }

    protected Operation createOperation(OperationType type, String keyspace, String clusterName, int port, String...hosts) {
        Operation operation = new Operation()
                .type(type);
        operation.putAnyNonEmpty(CassandraOperationFinalizer.KEYSPACE, keyspace);
        operation.putAnyNonEmpty(CassandraOperationFinalizer.CLUSTER_NAME, clusterName);
        operation.put(CassandraOperationFinalizer.PORT, port);
        OperationList list = operation.createList(CassandraOperationFinalizer.HOSTS);
        for(String host: hosts) {
            list.add(host);
        }
        return operation;

    }

    protected Frame createFrame(Operation op) {
        Frame frame = new SimpleFrame(FrameId.valueOf("0"),
                null,
                op,
                TimeRange.milliTimeRange(0, 1),
                Collections.<Frame>emptyList());
        return frame;
    }

}