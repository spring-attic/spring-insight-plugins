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

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationUtils;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.MapUtil;
import com.springsource.insight.util.StringUtil;

/**
 * Note: this aspect covers also {@link javax.net.ssl.HttpsURLConnection} since
 * it is derived from {@link HttpURLConnection}
 */
public aspect HttpURLConnectionOperationCollectionAspect
        extends SocketOperationCollectionAspectSupport {

    public HttpURLConnectionOperationCollectionAspect() {
        super();
    }

    // we have to use 'call' since HttpURLConnection is a core class
    public pointcut collectionPoint(): call(* HttpURLConnection+.connect());

    @Override
    protected Operation createOperation(JoinPoint jp) {
        HttpURLConnection conn = (HttpURLConnection) jp.getTarget();
        URL url = conn.getURL();
        String host = url.getHost();
        int port = url.getPort();
        if (port <= 0) {
            port = url.getDefaultPort();
        }

        Operation op = createOperation(super.createOperation(jp), SocketDefinitions.CONNECT_ACTION, host, port);
        op.put("method", conn.getRequestMethod())
                .put(OperationFields.URI, url.toExternalForm())
        ;
        if (collectExtraInformation()) {
            fillInMessageHeaders(op.createList("request"), conn.getRequestProperties());
        }

        return op;
    }

    OperationList fillInMessageHeaders(OperationList headers, Map<String, ? extends Collection<String>> hdrsMap) {
        if (MapUtil.size(hdrsMap) <= 0) {
            return headers;
        }

        SocketCollectOperationContext context = getSocketCollectOperationContext();
        for (Map.Entry<String, ? extends Collection<String>> hdrEntry : hdrsMap.entrySet()) {
            String name = hdrEntry.getKey();
            Collection<String> valsList = hdrEntry.getValue();
            if (ListUtil.size(valsList) <= 0) {
                continue;
            }

            for (String value : valsList) {
                if (StringUtil.isEmpty(value)) {
                    continue;
                }

                OperationUtils.addNameValuePair(headers, name, value);

                if (context.updateObscuredHeaderValue(name, value) && _logger.isLoggable(Level.FINE)) {
                    _logger.fine("fillInMessageHeaders(" + name + ") obscured: " + value);
                }
            }
        }

        return headers;
    }
}
