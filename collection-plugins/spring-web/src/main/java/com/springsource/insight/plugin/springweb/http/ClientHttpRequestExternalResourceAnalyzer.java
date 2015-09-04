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
import com.springsource.insight.intercept.trace.Trace;
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
            int port = resolvePort(uri);

            String color = colorManager.getColor(op);
            String name = host + ":" + port;
            return new ExternalResourceDescriptor(frame,
                    MD5NameGenerator.getName(name),
                    name,
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
    static int resolvePort(URI uri) {
        if (uri == null) {
            return (-1);
        }

        int port = uri.getPort();
        if (port <= 0) {
            if ("http".equals(uri.getScheme()))
                port = 80;
            else
                port = 443;
        }

        return port;
    }
}
