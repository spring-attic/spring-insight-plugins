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

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import com.springsource.insight.collection.DefaultOperationCollector;
import com.springsource.insight.collection.FrameBuilderHintObscuredValueMarker;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.operation.OperationUtils;
import com.springsource.insight.intercept.plugin.CollectionSettingName;
import com.springsource.insight.intercept.plugin.CollectionSettingsRegistry;
import com.springsource.insight.intercept.plugin.CollectionSettingsUpdateListener;
import com.springsource.insight.intercept.trace.FrameBuilder;
import com.springsource.insight.intercept.trace.ObscuredValueMarker;
import com.springsource.insight.util.StringFormatterUtils;

/**
 * 
 */
public class ClientHttpRequestOperationCollector extends DefaultOperationCollector {
	public static final OperationType	TYPE=OperationType.valueOf("spring_client_reqhttp");

    private static final InterceptConfiguration configuration = InterceptConfiguration.getInstance();
    // NOTE: using same value as apache client to enable mutual configuration
    protected static final CollectionSettingName    OBFUSCATED_HEADERS_SETTING =
            new CollectionSettingName("http.client.obfuscated.headers", "apache.http.client", "Comma separated list of headers whose data requires obfuscation");

    // see RFC(s) 2616-2617
    static final String DEFAULT_OBFUSCATED_HEADERS_LIST="Authorization,Authentication-Info,WWW-Authenticate";
    // NOTE: using a synchronized set in order to allow modification while running
    static final Set<String>    OBFUSCATED_HEADERS=
            Collections.synchronizedSet(new TreeSet<String>(String.CASE_INSENSITIVE_ORDER) {
                private static final long serialVersionUID = 1L;

                {
                    addAll(toHeaderNameSet(DEFAULT_OBFUSCATED_HEADERS_LIST));
                }
            });

    // register a collection setting update listener to update the obfuscated headers
    static {
        CollectionSettingsRegistry registry = CollectionSettingsRegistry.getInstance();
        registry.addListener(new CollectionSettingsUpdateListener() {
                public void incrementalUpdate (CollectionSettingName name, Serializable value) {
                    Logger   LOG=Logger.getLogger(ClientHttpRequestOperationCollector.class.getName());
                    if (OBFUSCATED_HEADERS_SETTING.equals(name) && (value instanceof String)) {
                       if (OBFUSCATED_HEADERS.size() > 0) { // check if replacing or populating
                           LOG.info("incrementalUpdate(" + name + ")" + OBFUSCATED_HEADERS + " => [" + value + "]");
                           OBFUSCATED_HEADERS.clear();
                       }

                       OBFUSCATED_HEADERS.addAll(toHeaderNameSet((String) value));
                    } else if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("incrementalUpdate(" + name + ")[" + value + "] ignored");
                    }
                }
            });
        registry.register(OBFUSCATED_HEADERS_SETTING, DEFAULT_OBFUSCATED_HEADERS_LIST);
    }

    private ObscuredValueMarker obscuredMarker =
            new FrameBuilderHintObscuredValueMarker(configuration.getFrameBuilder());

    public ClientHttpRequestOperationCollector() {
		super();
	}

    void setSensitiveValueMarker(ObscuredValueMarker marker) {
        this.obscuredMarker = marker;
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

		for (String key : hdrs.keySet()) {
			String	value=hdrs.getFirst(key);
            if (OBFUSCATED_HEADERS.contains(key)) {
                obscuredMarker.markObscured(value);
            }
            OperationUtils.addNameValuePair(op, key, value);
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
