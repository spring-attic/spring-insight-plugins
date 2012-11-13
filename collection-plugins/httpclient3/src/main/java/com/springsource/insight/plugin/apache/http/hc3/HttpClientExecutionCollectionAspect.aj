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
package com.springsource.insight.plugin.apache.http.hc3;

import java.io.IOException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.URIException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionUtil;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.collection.http.HttpHeadersObfuscator;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.color.ColorManager.ColorParams;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationUtils;
import com.springsource.insight.intercept.trace.FrameBuilder;
import com.springsource.insight.util.ArrayUtil;

/**
 * 
 */
public aspect HttpClientExecutionCollectionAspect extends OperationCollectionAspectSupport {
	private static final InterceptConfiguration	configuration=InterceptConfiguration.getInstance();
	private HttpHeadersObfuscator	obfuscator=HttpHeadersObfuscator.getInstance();

    public HttpClientExecutionCollectionAspect () {
        super();
    }

    HttpHeadersObfuscator getHttpHeadersObfuscator () {
    	return obfuscator;
    }

    void setHttpHeadersObfuscator (HttpHeadersObfuscator obfs) {
    	obfuscator = obfs;
    }

    @Override
    public String getPluginName() {
        return HC3PluginRuntimeDescriptor.PLUGIN_NAME;
    }

    public pointcut clientExecutionFlow ()
        : execution(* org.apache.commons.httpclient.HttpClient.executeMethod(HttpMethod))
       || execution(* org.apache.commons.httpclient.HttpClient.executeMethod(HostConfiguration,HttpMethod))
       || execution(* org.apache.commons.httpclient.HttpClient.executeMethod(HostConfiguration,HttpMethod,HttpState))
       ;

    @SuppressAjWarnings({"adviceDidNotMatch"})
    int around () throws IOException
        : clientExecutionFlow()
       && (!cflowbelow(clientExecutionFlow()))
       && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart)) {
        final Operation   op=enterOperation(thisJoinPointStaticPart);
        final HttpMethod  method=HttpPlaceholderMethod.resolveHttpMethod(thisJoinPoint.getArgs());

        colorForward(new ColorParams() {
			public void setColor(String key, String value) {
				method.addRequestHeader(key, value);
			}

            public Operation getOperation() {
				return op;
			}
		});

        try
        {
            int statusCode=proceed();
            exitOperation(op, method, statusCode, null);
            return statusCode;
        }
        catch(IOException e)
        {
            exitOperation(op, method, HttpClientDefinitions.FAILED_CALL_STATUS_CODE, e);
            throw e;
        }
        catch(RuntimeException e)
        {
            exitOperation(op, method, HttpClientDefinitions.FAILED_CALL_STATUS_CODE, e);
            throw e;
        }
    }
        
    /* -------------------------------------------------------------------- */

    Operation enterOperation (JoinPoint.StaticPart staticPart) {
        Operation           op=createOperation(staticPart);
        OperationCollector  collector=getCollector();
        collector.enter(op);
        return op;
    }

    Operation createOperation (JoinPoint.StaticPart staticPart) {
        return new Operation()
            .type(HttpClientDefinitions.TYPE)
            .sourceCodeLocation(OperationCollectionUtil.getSourceCodeLocation(staticPart))
            ;
    }

    Operation exitOperation (Operation op, HttpMethod method, int statusCode, Throwable e) {
        fillInOperation(op, method, statusCode);

        OperationCollector  collector=getCollector();
        if (e == null)
            collector.exitNormal(Integer.valueOf(statusCode));
        else
            collector.exitAbnormal(e);

        return op;
    }

    Operation fillInOperation (Operation op, HttpMethod method, int statusCode) {
        op.label(method.getName() + " " + getUri(method));

        boolean collectExtra = collectExtraInformation();
        fillInRequestDetails(op.createMap("request"), method, collectExtra);
        fillInResponseDetails(op.createMap("response"), method, statusCode, collectExtra);
        return op;
    }

    OperationMap fillInRequestDetails (OperationMap op, HttpMethod method, boolean collectExtra) {
        fillInRequestNetworkDetails(op, method, collectExtra);
        if (collectExtra) {
            fillInMethodHeaders(op.createList("headers"), method, true);
        }
        return op;
    }

    OperationMap fillInRequestNetworkDetails (OperationMap op, HttpMethod method, boolean collectExtra) {
        op.put("method", method.getName())
          .put(OperationFields.URI, getUri(method));

        if (collectExtra) {
            op.putAnyNonEmpty("protocol", createVersionValue(method));
        }

        return op;
    }

    static String createVersionValue (HttpMethod method) {
        StatusLine  line=method.getStatusLine();
        
        if (line != null) {
        	return line.getHttpVersion();
        } else {
        	return null;
        }
    }

    static String getUri (HttpMethod method) {
        try {
            return String.valueOf(method.getURI());
        } catch(URIException e) {
            throw new RuntimeException("Failed to get URI of " + method, e);
        }
    }

    OperationMap fillInResponseDetails(OperationMap op, HttpMethod method, int statusCode, boolean collectExtra) {
        op.put(HttpStatusTraceErrorAnalyzer.STATUS_CODE_ATTR, statusCode);

        if (collectExtra) {
            op.putAnyNonEmpty(HttpStatusTraceErrorAnalyzer.REASON_PHRASE_ATTR, method.getStatusText());
            fillInMethodHeaders(op.createList("headers"), method, false);
        }

        return op;
    }

    OperationList fillInMethodHeaders (OperationList headers, HttpMethod method, boolean useRequestHeaders) {
        Header[]    hdrs=useRequestHeaders ? method.getRequestHeaders() : method.getResponseHeaders(); 
        if (ArrayUtil.length(hdrs) <= 0) {
            return headers;
        }

        HttpHeadersObfuscator obfs=getHttpHeadersObfuscator();
        for (Header h : hdrs) {
            String  name=h.getName(), value=h.getValue();

            OperationUtils.addNameValuePair(headers, name, value);
            if (obfs.processHeader(name, value)) {
            	continue;	// debug breakpoint
            }
        }

        return headers;
    }

    boolean collectExtraInformation () {
        return FrameBuilder.OperationCollectionLevel.HIGH.equals(configuration.getCollectionLevel());
    }
}
