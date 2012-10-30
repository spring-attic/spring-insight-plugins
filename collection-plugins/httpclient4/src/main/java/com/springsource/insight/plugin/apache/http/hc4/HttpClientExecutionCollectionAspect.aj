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
package com.springsource.insight.plugin.apache.http.hc4;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import com.springsource.insight.collection.FrameBuilderHintObscuredValueMarker;
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionUtil;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.color.ColorManager.ColorParams;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationUtils;
import com.springsource.insight.intercept.plugin.CollectionSettingName;
import com.springsource.insight.intercept.plugin.CollectionSettingsRegistry;
import com.springsource.insight.intercept.plugin.CollectionSettingsUpdateListener;
import com.springsource.insight.intercept.trace.FrameBuilder;
import com.springsource.insight.intercept.trace.ObscuredValueMarker;

/**
 * 
 */
public aspect HttpClientExecutionCollectionAspect extends OperationCollectionAspectSupport {
    private static final InterceptConfiguration configuration = InterceptConfiguration.getInstance();

    // NOTE: using same value as version 3.0 to enable mutual configuration
    protected static final CollectionSettingName    OBFUSCATED_HEADERS_SETTING =
            new CollectionSettingName("obfuscated.headers", "apache.http.client", "Comma separated list of headers whose data requires obfuscation");
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
                   Logger   LOG=Logger.getLogger(HttpClientExecutionCollectionAspect.class.getName());
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
    public HttpClientExecutionCollectionAspect () {
        super();
    }

    void setSensitiveValueMarker(ObscuredValueMarker marker) {
        this.obscuredMarker = marker;
    }

    public pointcut withResponseExecution()
        : execution(* org.apache.http.client.HttpClient.execute(HttpUriRequest))
       || execution(* org.apache.http.client.HttpClient.execute(HttpUriRequest,HttpContext))
       || execution(* org.apache.http.client.HttpClient.execute(HttpHost,HttpRequest))
       || execution(* org.apache.http.client.HttpClient.execute(HttpHost,HttpRequest,HttpContext))
        ;

    @SuppressAjWarnings({"adviceDidNotMatch"})
    HttpResponse around () throws IOException
            : withResponseExecution()
           && (!cflowbelow(withResponseExecution()))
           && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart)) {
        final Operation    op=enterOperation(thisJoinPointStaticPart);
        final HttpRequest  request=HttpPlaceholderRequest.resolveHttpRequest(thisJoinPoint.getArgs());
        
        colorForward(new ColorParams() {
			public void setColor(String key, String value) {
				request.addHeader(key, value);
				}

			public Operation getOperation() {
				return op;
			}
		});

        try
        {
            HttpResponse    response=proceed();
            exitOperation(op, request, response, null);
            return response;
        }
        catch(IOException e)
        {
            exitOperation(op, request, null, e);
            throw e;
        }
        catch(RuntimeException e)
        {
            exitOperation(op, request, null, e);
            throw e;
        }
    }

    /* -------------------------------------------------------------------- */

    public pointcut withResponseHandlerExecutionFlow ()
        : execution(* org.apache.http.client.HttpClient.execute(..,ResponseHandler,..))
        ;

    @SuppressAjWarnings({"adviceDidNotMatch"})
    Object around (ResponseHandler<?> handler) throws IOException
            : withResponseHandlerExecutionFlow()
           && (!cflowbelow(withResponseHandlerExecutionFlow()))
           && args(handler)
           && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart)) {
        final Operation     op=enterOperation(thisJoinPointStaticPart);
        Object[]            args=thisJoinPoint.getArgs();
        final HttpRequest   request=HttpPlaceholderRequest.resolveHttpRequest(args);

        colorForward(new ColorParams() {
			public void setColor(String key, String value) {
				request.addHeader(key, value);
			}

            public Operation getOperation() {
				return op;
			}
		});

        // must be final or the anonymous class cannot reference it...
        final AtomicReference<HttpResponse> rspRef=new AtomicReference<HttpResponse>(null);
        final ResponseHandler<?>            rspHandler=handler;
        // if resolution yielded the placeholder no need to create a wrapper
        ResponseHandler<?>                  wrapper=(request == HttpPlaceholderRequest.PLACEHOLDER)
                ? handler : new ResponseHandler<Object>() {
                        public Object handleResponse(HttpResponse response) throws IOException {
                            HttpResponse    prevValue=rspRef.getAndSet(response);
                            if (prevValue != null) {
                                throw new IllegalStateException("Multiple responses: current=" + response + "/previous=" + prevValue);
                            }
                            return rspHandler.handleResponse(response);
                        }
                    };
        try
        {
            Object          returnValue=proceed(wrapper);
            HttpResponse    response=rspRef.get();
            if (response == null) {
                throw new IllegalStateException("No response provided during handler invocation");
            }
            exitOperation(op, request, response, null);
            return returnValue;
        }
        catch(IOException e)
        {
            exitOperation(op, request, null, e);
            throw e;
        }
        catch(RuntimeException e)
        {
            exitOperation(op, request, null, e);
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

    Operation exitOperation (Operation op, HttpRequest request, HttpResponse response, Throwable e) {
        fillInOperation(op, request, response);

        OperationCollector  collector=getCollector();
        if (e == null)
            collector.exitNormal(response);
        else
            collector.exitAbnormal(e);

        return op;
    }

    Operation fillInOperation (Operation op, HttpRequest request, HttpResponse response) {
        RequestLine     reqLine=request.getRequestLine();
        op.label(reqLine.getMethod() + " " + reqLine.getUri());

        boolean         collectExtra = collectExtraInformation();
        fillInRequestDetails(op.createMap("request"), request, collectExtra);
        if (response != null) {
            fillInResponseDetails(op.createMap("response"), response, collectExtra);
        }
        return op;
    }

    OperationMap fillInRequestDetails (OperationMap op, HttpRequest request, boolean collectExtra) {
        fillInRequestNetworkDetails(op, request, collectExtra);
        if (collectExtra) {
            fillInMessageHeaders(op.createList("headers"), request);
        }
        return op;
    }

    OperationMap fillInRequestNetworkDetails (OperationMap op, HttpRequest request, boolean collectExtra) {
        RequestLine     reqLine=request.getRequestLine();
        op.put("method", reqLine.getMethod())
          .put(OperationFields.URI, reqLine.getUri());

        if (collectExtra) {
            op.put("protocol", createVersionValue(reqLine.getProtocolVersion()));
        }

        return op;
    }

    static String createVersionValue (ProtocolVersion protoVersion) {
        return protoVersion.getProtocol() + "/" + String.valueOf(protoVersion.getMajor()) + "." + String.valueOf(protoVersion.getMinor());
    }

    OperationMap fillInResponseDetails(OperationMap op, HttpResponse response, boolean collectExtra) {
        StatusLine statusLine = response.getStatusLine();
        op.put("statusCode", statusLine.getStatusCode());

        if (collectExtra) {
            op.putAnyNonEmpty("reasonPhrase", statusLine.getReasonPhrase());
            fillInMessageHeaders(op.createList("headers"), response);
        }

        return op;
    }

    OperationList fillInMessageHeaders (OperationList headers, HttpMessage msg)
    {
        Header[]    hdrs=msg.getAllHeaders();
        if ((hdrs == null) || (hdrs.length <= 0)) {
            return headers;
        }

        for (Header h : hdrs) {
            String  name=h.getName(), value=h.getValue();
            if (OBFUSCATED_HEADERS.contains(name)) {
                obscuredMarker.markObscured(value);
            }
            OperationUtils.addNameValuePair(headers, name, value);
        }

        return headers;
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

    boolean collectExtraInformation ()
    {
        return FrameBuilder.OperationCollectionLevel.HIGH.equals(configuration.getCollectionLevel());
    }

    @Override
    public String getPluginName() {
        return HC4PluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
