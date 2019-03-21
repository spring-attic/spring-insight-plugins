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

package com.springsource.insight.plugin.springweb.remoting;

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
import com.springsource.insight.plugin.springweb.SpringWebHelpers;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;

/**
 *
 */
public class HttpInvokerRequestExecutorExternalResourceAnalyzer extends AbstractExternalResourceAnalyzer {
    public static final OperationType HTTP_INVOKER = OperationType.valueOf("http_invoker");
    /**
     * Special attribute used to indicate whether the HTTP invocation was
     * executed using core Java classes (e.g. {@link java.net.HttpURLConnection}
     * only or via a framework (e.g., <A
     * HREF="https://hc.apache.org/httpclient-3.x/">Apache client</A>). We
     * generate an external resource only for the <U>core</U> classes invocation
     * and rely on the other plugins for the alternative frameworks. This is
     * done in order to avoid ambiguity if both the HTTP invoker aspect and the
     * framework plugin are applied to the same trace, and thus may generate
     * equivalent (though not same) external resource descriptors
     *
     * @see org.springframework.remoting.httpinvoker.SimpleHttpInvokerRequestExecutor
     */
    public static final String DIRECT_CALL_ATTR = "directInvocationCall";
    public static final int IPPORT_HTTP = 80;
    private static final HttpInvokerRequestExecutorExternalResourceAnalyzer INSTANCE = new HttpInvokerRequestExecutorExternalResourceAnalyzer();

    private HttpInvokerRequestExecutorExternalResourceAnalyzer() {
        super(HTTP_INVOKER);
    }

    public static final HttpInvokerRequestExecutorExternalResourceAnalyzer getInstance() {
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
                continue; // debug breakpoint
            }
        }

        return descs;
    }

    ExternalResourceDescriptor extractExternalResourceDescriptor(Frame frame) {
        Operation op = frame.getOperation();
        Boolean directCall = op.get(DIRECT_CALL_ATTR, Boolean.class);
        if ((directCall == null) || (!directCall.booleanValue())) {
            return null;
        }

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
            return new ExternalResourceDescriptor(frame,
                    name,
                    hostPort,
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
