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
package com.springsource.insight.plugin.neo4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.topology.AbstractExternalResourceAnalyzer;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;


public abstract class Neo4JExternalResourceAnalyzer extends AbstractExternalResourceAnalyzer {

    private static String NEO4J_VENDOR = "Neo4J";
    protected Neo4JExternalResourceAnalyzer(OperationType type) {
        super(type);
    }

    public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace, Collection<Frame> frames) {
        if (ListUtil.size(frames) <= 0) {
            return Collections.emptyList();
        }

        List<ExternalResourceDescriptor> queueDescriptors = new ArrayList<ExternalResourceDescriptor>(frames.size());
        for (Frame cacheFrame : frames) {
            Operation op = cacheFrame.getOperation();
            String service = op.get("service", String.class);
            if (StringUtil.isEmpty(service))
                continue;

            String hashString = MD5NameGenerator.getName(service);
            String color = colorManager.getColor(op);
            String label = null;

            ExternalResourceType resType = ExternalResourceType.GRAPH_DATABASE;
            if (service.indexOf("EmbeddedGraphDatabase") >= 0) {
                resType = ExternalResourceType.FILESTORE;
                label = service;
            }

            int port = -1;
            String host = "localhost";
            if (resType == ExternalResourceType.GRAPH_DATABASE) {
                try {
                    URI url = new URI(service);
                    host = url.getHost();
                    port = url.getPort();

                } catch (URISyntaxException e) {
                    // invalid uri
                }
                label =  host + "-" + port;
            }

            ExternalResourceDescriptor descriptor =
                    new ExternalResourceDescriptor(cacheFrame, "neo4j:" + hashString, label,
                            resType.name(), NEO4J_VENDOR,
                            host, port, color, false);
            queueDescriptors.add(descriptor);
        }

        return queueDescriptors;
    }
}
