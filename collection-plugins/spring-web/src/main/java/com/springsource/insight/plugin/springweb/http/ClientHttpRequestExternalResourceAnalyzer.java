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

package com.springsource.insight.plugin.springweb.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.topology.AbstractExternalResourceAnalyzer;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameUtil;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.plugin.springweb.SpringWebHelpers;
import com.springsource.insight.plugin.springweb.rest.RestOperationExternalResourceAnalyzer;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;

/**
 *
 */
public class ClientHttpRequestExternalResourceAnalyzer extends AbstractExternalResourceAnalyzer {
    public static final OperationType TYPE = OperationType.valueOf("spring_client_reqhttp");
    public static final int IPPORT_HTTP = 80;
    private static final ClientHttpRequestExternalResourceAnalyzer INSTANCE = new ClientHttpRequestExternalResourceAnalyzer();

    private ClientHttpRequestExternalResourceAnalyzer() {
        super(TYPE);
    }

    public static final ClientHttpRequestExternalResourceAnalyzer getInstance() {
        return INSTANCE;
    }

    public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace, Collection<Frame> externalFrames) {
        if (ListUtil.size(externalFrames) <= 0) {
            return Collections.emptyList();
        }

        Set<ExternalResourceDescriptor> descs = new HashSet<ExternalResourceDescriptor>(externalFrames.size());
        for (Frame frame : externalFrames) {
            ExternalResourceDescriptor extDesc = extractExternalResourceDescriptor(frame);
            if (extDesc == null) {
                continue;
            }

            if (!descs.add(extDesc)) {
                continue;    // debug breakpoint
            }
        }

        return descs;
    }

    ExternalResourceDescriptor extractExternalResourceDescriptor(Frame frame) {
        Operation op = frame.getOperation();
        String url = op.get(OperationFields.URI, String.class);
        if (StringUtil.isEmpty(url)) {
            return null;
        }

        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            int port = SpringWebHelpers.resolvePort(uri);

            String color = colorManager.getColor(op);

            String hostPort = host + ":" + port;
            String name = SpringWebHelpers.createName(hostPort);
            String lbl = hostPort;

            Operation rootFrameOperation = SpringWebHelpers.getRootFrameOperation(frame);
            if (rootFrameOperation != null) {
                String unresolvedURI = SpringWebHelpers.findUnresolvedURI(rootFrameOperation, url);
                if (!StringUtil.isEmpty(unresolvedURI)) {
                    try {
                        URI origuri = new URI(SpringWebHelpers.sanitizeURI(unresolvedURI));
                        lbl = origuri.getHost();
                    } catch (URISyntaxException e) {
                        // Ignore, use other label
                    }
                }
            }

            return new ExternalResourceDescriptor(frame,
                    name,
                    lbl,
                    ExternalResourceType.WEB_SERVER.name(),
                    null,
                    host,
                    port,
                    color,
                    false);
        } catch (URISyntaxException e) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("createExternalResourceDescriptor(" + url + "): " + e.getMessage());
            }

            return null;
        }
    }
}
