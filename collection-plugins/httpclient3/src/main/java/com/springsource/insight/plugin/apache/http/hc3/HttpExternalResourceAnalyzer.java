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
package com.springsource.insight.plugin.apache.http.hc3;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.springsource.insight.intercept.endpoint.EndPointAnalyzersRegistry;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationUtils;
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
public class HttpExternalResourceAnalyzer extends AbstractExternalResourceAnalyzer {
    private static final HttpExternalResourceAnalyzer INSTANCE = new HttpExternalResourceAnalyzer();

    private HttpExternalResourceAnalyzer() {
        super(HttpClientDefinitions.TYPE);
    }

    public static final HttpExternalResourceAnalyzer getInstance() {
        return INSTANCE;
    }

    public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace, Collection<Frame> framesList) {
        if (ListUtil.size(framesList) <= 0) {
            return Collections.emptyList();
        }

        Set<ExternalResourceDescriptor> resSet = new HashSet<ExternalResourceDescriptor>(framesList.size());
        for (Frame frame : framesList) {
            ExternalResourceDescriptor res = extractExternalResourceDescriptor(frame);
            if (res == null) { // can happen if failed to parse the URI somehow
                continue;
            }

            if (!resSet.add(res))
                continue; // debug breakpoint
        }

        return resSet;
    }

    ExternalResourceDescriptor extractExternalResourceDescriptor(Frame frame) {
        Operation op = (frame == null) ? null : frame.getOperation();
        OperationMap requestDetails = (op == null) ? null : op.get("request", OperationMap.class);
        String uriValue = (requestDetails == null) ? null : requestDetails.get(OperationFields.URI, String.class);

        if (StringUtil.getSafeLength(uriValue) <= 0) {
            return null;
        }

        try
        {
            URI uri = new URI(uriValue);
            String host = uri.getHost();
            int port = resolvePort(uri);
            String color = colorManager.getColor(op);
            String app = null;
            String ser = null;
            String ep = null;

            OperationMap responseDetails = (op == null) ? null : op.get("response", OperationMap.class);

            if (responseDetails != null) {
                OperationList headersList = responseDetails.get("headers", OperationList.class);

                if (headersList != null) {
                    for (int i = 0; i < headersList.size(); i++) {
                        OperationMap map = headersList.get(i, OperationMap.class);

                        String headerName = map.get(OperationUtils.NAME_KEY, String.class);
                        String headerValue = map.get(OperationUtils.VALUE_KEY, String.class);

                        if ((app != null) && EndPointAnalyzersRegistry.APP_TOKEN_NAME.equals(headerName)) {
                            app = headerValue;
                        } else if ((ser != null) && EndPointAnalyzersRegistry.SERVER_TOKEN_NAME.equals(headerName)) {
                            ser = headerValue;
                        } else if ((ep != null) && EndPointAnalyzersRegistry.TOKEN_NAME.equals(headerName)) {
                            ep = headerValue;
                        }

                        if ((app != null) && (ser != null) && (ep != null)) {
                            break;
                        }
                    }
                }
            }

            String lbl = host + ":" + port;
            String name = createName(lbl, app, ser, ep);

            return new ExternalResourceDescriptor(frame, name,
                    lbl, // label
                    ExternalResourceType.WEB_SERVER.name(),
                    null, // vendor
                    host,
                    port,
                    color, false,
                    app, ser, ep, null);
        } catch (URISyntaxException e) {
            Logger logger = Logger.getLogger(getClass().getName());
            logger.warning("Failed to parse " + uriValue + ": " + e.getMessage());
            return null;
        }
    }

    static String createName(String lbl, String app, String ser, String ep) {
        StringBuilder sb = new StringBuilder(lbl);

        if (!StringUtil.isEmpty(app) && !StringUtil.isEmpty(ser) && !StringUtil.isEmpty(ep)) {
            sb.append(app);
            sb.append(ser);
            sb.append(ep);
        }

        return MD5NameGenerator.getName(sb.toString());
    }

    static int resolvePort(URI uri) {
        if (uri == null) {
            return (-1);
        }

        int port = uri.getPort();
        if (port <= 0) {
            return 80;
        }

        return port;
    }
}
