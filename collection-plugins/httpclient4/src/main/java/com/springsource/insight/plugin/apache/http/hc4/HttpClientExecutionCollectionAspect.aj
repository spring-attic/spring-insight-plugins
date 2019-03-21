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
package com.springsource.insight.plugin.apache.http.hc4;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import com.springsource.insight.intercept.trace.TraceId;
import com.springsource.insight.util.StringUtil;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionUtil;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.collection.http.HttpObfuscator;
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
    private static final InterceptConfiguration configuration = InterceptConfiguration.getInstance();
    private HttpObfuscator obfuscator = HttpObfuscator.getInstance();

    public HttpClientExecutionCollectionAspect() {
        super();
    }


    public pointcut withResponseExecution()
            : execution(* org.apache.http.client.HttpClient.execute(HttpUriRequest))
            || execution(* org.apache.http.client.HttpClient.execute(HttpUriRequest,HttpContext))
            || execution(* org.apache.http.client.HttpClient.execute(HttpHost,HttpRequest))
            || execution(* org.apache.http.client.HttpClient.execute(HttpHost,HttpRequest,HttpContext))
            ;

    HttpObfuscator getHttpHeadersObfuscator() {
        return obfuscator;
    }

    void setHttpHeadersObfuscator(HttpObfuscator obfs) {
        obfuscator = obfs;
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    Object around ()throws IOException
            : withResponseExecution()
            && (!cflowbelow(withResponseExecution()))
            && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart)) {
        final Operation op = enterOperation(thisJoinPointStaticPart);
        final HttpRequest request = HttpPlaceholderRequest.resolveHttpRequest(thisJoinPoint.getArgs());

        colorForward(new ColorParams() {
            public void setColor(String key, String value) {
                request.addHeader(key, value);
            }

            public Operation getOperation() {
                return op;
            }
        });

        try {
            Object response = proceed();
            exitOperation(op, request, (HttpResponse)response, null);
            return response;
        } catch (IOException e) {
            exitOperation(op, request, null, e);
            throw e;
        } catch (RuntimeException e) {
            exitOperation(op, request, null, e);
            throw e;
        }
    }

    /* -------------------------------------------------------------------- */

    public pointcut withResponseHandlerExecutionFlow()
            : execution(* org.apache.http.client.HttpClient.execute(..,ResponseHandler,..))
            ;

    @SuppressAjWarnings({"adviceDidNotMatch"})
    Object around (ResponseHandler<?> handler)throws IOException
            : withResponseHandlerExecutionFlow()
            && (!cflowbelow(withResponseHandlerExecutionFlow()))
            && args(handler)
            && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart)) {
        final Operation op = enterOperation(thisJoinPointStaticPart);
        Object[] args = thisJoinPoint.getArgs();
        final HttpRequest request = HttpPlaceholderRequest.resolveHttpRequest(args);

        colorForward(new ColorParams() {
            public void setColor(String key, String value) {
                request.addHeader(key, value);
            }

            public Operation getOperation() {
                return op;
            }
        });

        // must be final or the anonymous class cannot reference it...
        final AtomicReference<HttpResponse> rspRef = new AtomicReference<HttpResponse>(null);
        final ResponseHandler<?> rspHandler = handler;
        // if resolution yielded the placeholder no need to create a wrapper
        ResponseHandler<?> wrapper = (request == HttpPlaceholderRequest.PLACEHOLDER)
                ? handler : new ResponseHandler<Object>() {
            public Object handleResponse(HttpResponse response) throws IOException {
                HttpResponse prevValue = rspRef.getAndSet(response);
                if (prevValue != null) {
                    throw new IllegalStateException("Multiple responses: current=" + response + "/previous=" + prevValue);
                }
                return rspHandler.handleResponse(response);
            }
        };
        try {
            Object returnValue = proceed(wrapper);
            HttpResponse response = rspRef.get();
            if (response == null) {
                throw new IllegalStateException("No response provided during handler invocation");
            }
            exitOperation(op, request, response, null);
            return returnValue;
        } catch (IOException e) {
            exitOperation(op, request, null, e);
            throw e;
        } catch (RuntimeException e) {
            exitOperation(op, request, null, e);
            throw e;
        }
    }

    /* -------------------------------------------------------------------- */

    Operation enterOperation(JoinPoint.StaticPart staticPart) {
        Operation op = createOperation(staticPart);
        OperationCollector collector = getCollector();
        collector.enter(op);
        return op;
    }

    Operation createOperation(JoinPoint.StaticPart staticPart) {
        return new Operation()
                .type(HttpClientDefinitions.TYPE)
                .sourceCodeLocation(OperationCollectionUtil.getSourceCodeLocation(staticPart))
                ;
    }

    Operation exitOperation(Operation op, HttpRequest request, HttpResponse response, Throwable e) {
        fillInOperation(op, request, response);

        OperationCollector collector = getCollector();
        if (e == null)
            collector.exitNormal(response);
        else
            collector.exitAbnormal(e);

        return op;
    }

    Operation fillInOperation(Operation op, HttpRequest request, HttpResponse response) {
        RequestLine reqLine = request.getRequestLine();
        op.label(reqLine.getMethod() + " " + reqLine.getUri());

        boolean collectExtra = collectExtraInformation();
        fillInRequestDetails(op.createMap("request"), request, collectExtra);
        if (response != null) {
            fillInResponseDetails(op.createMap("response"), response, collectExtra);
            updateExternalTraceDetails(op, response);
        }
        return op;
    }

    private void updateExternalTraceDetails(Operation op, HttpMessage msg) {
        Header[] hdrs = msg.getAllHeaders();
        if (ArrayUtil.length(hdrs) <= 0) {
            return;
        }

        for (Header h : hdrs) {
            String name = h.getName(), value = h.getValue();
            if (TraceId.TRACE_ID_HEADER_NAME.equalsIgnoreCase(name)) {
                op.put(OperationFields.EXTERNAL_TRACE_ID, value);
            }
        }
    }

    OperationMap fillInRequestDetails(OperationMap op, HttpRequest request, boolean collectExtra) {
        fillInRequestNetworkDetails(op, request, collectExtra);
        if (collectExtra) {
            fillInMessageHeaders(op.createList("headers"), request);
        }
        return op;
    }

    OperationMap fillInRequestNetworkDetails(OperationMap op, HttpRequest request, boolean collectExtra) {
        RequestLine reqLine = request.getRequestLine();
        op.put("method", reqLine.getMethod())
                .put(OperationFields.URI, reqLine.getUri());

        if (collectExtra) {
            op.putAnyNonEmpty("protocol", createVersionValue(reqLine.getProtocolVersion()));
        }

        return op;
    }

    static String createVersionValue(ProtocolVersion protoVersion) {
        if (protoVersion == null) {
            return null;
        }

        return protoVersion.getProtocol() + "/" + String.valueOf(protoVersion.getMajor()) + "." + String.valueOf(protoVersion.getMinor());
    }

    OperationMap fillInResponseDetails(OperationMap op, HttpResponse response, boolean collectExtra) {
        StatusLine statusLine = response.getStatusLine();

        if (statusLine != null) {
            op.put("statusCode", statusLine.getStatusCode());
        }
        if (collectExtra) {
            if (statusLine != null) {
                op.putAnyNonEmpty("reasonPhrase", statusLine.getReasonPhrase());
            }
            fillInMessageHeaders(op.createList("headers"), response);
        }

        return op;
    }

    OperationList fillInMessageHeaders(OperationList headers, HttpMessage msg) {
        Header[] hdrs = msg.getAllHeaders();
        if (ArrayUtil.length(hdrs) <= 0) {
            return headers;
        }

        HttpObfuscator obfs = getHttpHeadersObfuscator();
        for (Header h : hdrs) {
            String name = h.getName(), value = h.getValue();
            OperationUtils.addNameValuePair(headers, name, value);
            if (obfs.processHeader(name, value)) {
                continue;    // debug breakpoint
            }
        }

        return headers;
    }

    boolean collectExtraInformation() {
        return FrameBuilder.OperationCollectionLevel.HIGH.equals(configuration.getCollectionLevel());
    }

    @Override
    public String getPluginName() {
        return HC4PluginRuntimeDescriptor.PLUGIN_NAME;
    }

    @Override
    public boolean isMetricsGenerator() {
        return true; // This provides an external resource
    }
}
