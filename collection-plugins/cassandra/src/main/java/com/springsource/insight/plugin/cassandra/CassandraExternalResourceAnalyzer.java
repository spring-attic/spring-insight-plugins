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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.color.ColorManager;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.topology.AbstractExternalResourceAnalyzer;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;

public class CassandraExternalResourceAnalyzer extends AbstractExternalResourceAnalyzer {

    public static final OperationType TYPE = OperationType.valueOf("cassandra");
    public static final String VENDOR = "cassandra";
    public static final String DEFAULT_CLUSTER_NAME = "UNNAMED_CLUSTER";
    public static final String DEFAULT_KEYSPACE_NAME = "UNKNOWN_KEYSPACE";

    private static final CassandraExternalResourceAnalyzer INSTANCE = new CassandraExternalResourceAnalyzer();

    private CassandraExternalResourceAnalyzer() {
        super(TYPE);
    }

    // package visibility for unit tests
    CassandraExternalResourceAnalyzer(OperationType type) {
        super(type);
    }

    public static final CassandraExternalResourceAnalyzer getInstance() {
        return INSTANCE;
    }

    public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace, Collection<Frame> dbFrames) {
        if (ListUtil.size(dbFrames) <= 0) {
            return Collections.emptyList();
        }

        List<ExternalResourceDescriptor> dbDescriptors = new ArrayList<ExternalResourceDescriptor>(dbFrames.size());
        for (Frame dbFrame : dbFrames) {
            Operation op = dbFrame.getOperation();
            String clusterName = op.get(CassandraOperationFinalizer.CLUSTER_NAME, String.class);
            String keyspace = op.get(CassandraOperationFinalizer.KEYSPACE, String.class);
            int port = op.getInt(CassandraOperationFinalizer.PORT, -1);
            OperationList hosts = op.get(CassandraOperationFinalizer.HOSTS, OperationList.class);

            dbDescriptors.add(getDescriptor(dbFrame, hosts, port, clusterName, keyspace));
        }

        return dbDescriptors;
    }


    private ExternalResourceDescriptor getDescriptor(Frame frame, OperationList hosts, int port, String clusterName, String keyspace) {

        ColorManager colorManager = ColorManager.getInstance();
        Operation op = frame.getOperation();
        String color = colorManager.getColor(op);

        if (StringUtil.isEmpty(clusterName))
            clusterName = DEFAULT_CLUSTER_NAME;

        clusterName = clusterName.replace(" ","_");
        if (StringUtil.isEmpty(keyspace))
            keyspace = DEFAULT_KEYSPACE_NAME;

        String host = "127.0.0.1";
        if (hosts != null && hosts.size() > 0 )
            host = hosts.get(0, String.class);

        ExternalResourceDescriptor descriptor = new ExternalResourceDescriptor(frame,
                VENDOR + ":" + clusterName + ":" + keyspace,
                keyspace,
                ExternalResourceType.MAPSTORE.name(),
                VENDOR,
                clusterName,
                port,
                color, false);

        return descriptor;
    }

}
