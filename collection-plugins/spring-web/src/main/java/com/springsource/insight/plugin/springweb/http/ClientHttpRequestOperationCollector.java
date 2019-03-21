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

package com.springsource.insight.plugin.springweb.http;

import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import com.springsource.insight.collection.DefaultOperationCollector;
import com.springsource.insight.collection.http.HttpObfuscator;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.operation.OperationUtils;
import com.springsource.insight.intercept.trace.FrameBuilder;
import com.springsource.insight.util.StringFormatterUtils;

/**
 * 
 */
public class ClientHttpRequestOperationCollector extends DefaultOperationCollector {
    public static final OperationType	TYPE=OperationType.valueOf("spring_client_reqhttp");
    private static final InterceptConfiguration	configuration=InterceptConfiguration.getInstance();
    private HttpObfuscator obfuscator = HttpObfuscator.getInstance();

    public ClientHttpRequestOperationCollector() {
        super();
    }

    HttpObfuscator getHttpObfuscator() {
        return obfuscator;
    }

    void setHttpObfuscator(HttpObfuscator obfs) {
        obfuscator = obfs;
    }

    @Override
    protected void processNormalExit(Operation op, Object returnValue) {
        if (!(returnValue instanceof ClientHttpResponse)) {
            return;
        }

        fillResponseDetails(op, (ClientHttpResponse) returnValue);
    }

    Operation fillResponseDetails(Operation op, ClientHttpResponse response) {
        fillInResponseDetails(op.createMap("response"), response);
        return op;
    }

    OperationMap fillInResponseDetails(OperationMap op, ClientHttpResponse response) {
        try {
            op.put(ClientHttpRequestTraceErrorAnalyzer.STATUS_CODE_ATTR, response.getRawStatusCode());
        } catch(IOException e) {
            op.put(ClientHttpRequestTraceErrorAnalyzer.STATUS_CODE_ATTR, -1);
        }

        if (collectExtraInformation()) {
            try {
                op.putAnyNonEmpty(ClientHttpRequestTraceErrorAnalyzer.REASON_PHRASE_ATTR, response.getStatusText());
            } catch(IOException e) {
                op.putAnyNonEmpty(ClientHttpRequestTraceErrorAnalyzer.REASON_PHRASE_ATTR, StringFormatterUtils.formatStackTrace(e));
            }
            fillMethodHeaders(op.createList("headers"), response.getHeaders());
        }

        return op;
    }

    Operation fillRequestDetails(Operation op, ClientHttpRequest request) {
        URI			uri=request.getURI();
        HttpMethod	method=request.getMethod();
        op.label(method + " " + uri.toString());
        fillRequestDetails(op.createMap("request"), request);
        return op;
    }

    OperationMap fillRequestDetails(OperationMap op, ClientHttpRequest request) {
        URI			uri=request.getURI();
        HttpMethod	method=request.getMethod();
        op.put(OperationFields.URI, uri.toString())
        .put("method", method.name())
        ;

        if (collectExtraInformation()) {
            fillMethodHeaders(op.createList("headers"), request.getHeaders());
        }

        return op;
    }

    OperationList fillMethodHeaders(OperationList op, HttpHeaders hdrs) {
        if (hdrs.isEmpty()) {
            return op;
        }

        HttpObfuscator obfs = getHttpObfuscator();
        for (String key : hdrs.keySet()) {
            String	value=hdrs.getFirst(key);
            OperationUtils.addNameValuePair(op, key, value);
            if (obfs.processHeader(key, value)) {
                continue;	// debug breakpoint
            }
        }

        return op;
    }

    boolean collectExtraInformation () {
        return FrameBuilder.OperationCollectionLevel.HIGH.equals(configuration.getCollectionLevel());
    }

    static Set<String> toHeaderNameSet (String value) {
        Set<String> result=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        String[]    names=value.split(",");
        for (String headerName : names) {
            String   trimmedValue=headerName.trim(); // in case extra whitespace
            if (trimmedValue.length() > 0) {
                result.add(trimmedValue);
            }
        }
        return result;
    }
}
