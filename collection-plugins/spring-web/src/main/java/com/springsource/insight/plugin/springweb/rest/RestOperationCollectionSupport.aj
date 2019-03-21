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

package com.springsource.insight.plugin.springweb.rest;

import java.net.URI;
import java.net.URL;

import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameBuilder;
import com.springsource.insight.intercept.trace.FrameUtil;
import com.springsource.insight.plugin.springweb.SpringWebHelpers;
import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.OperationCollectionUtil;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.plugin.springweb.AbstractSpringWebAspectSupport;
import com.springsource.insight.util.StringUtil;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.HttpMethod;

/**
 *
 */
public abstract aspect RestOperationCollectionSupport extends AbstractSpringWebAspectSupport {
    /**
     * Placeholder value used if unable to resolve the operation URI value
     * @see #resolveOperationURI(JoinPoint)
     */
    public static final String UNKNOWN_URI = "uri:unknown";


    private final String defaultMethod;

    protected RestOperationCollectionSupport(String accessType) {
        super(new RestOperationCollector());

        if (StringUtil.isEmpty(accessType)) {
            throw new IllegalStateException("No access method specified");
        }

        defaultMethod = accessType;
    }

    public pointcut requestCreation() :
        execution(* org.springframework.http.client.support.HttpAccessor+.createRequest(URI, HttpMethod))
            && cflowbelow(accessPoint())
            ;

    @SuppressAjWarnings("adviceDidNotMatch")
    after() returning(Object returnValue) : requestCreation() {
        if (returnValue instanceof ClientHttpRequest) {
            ClientHttpRequest clientHttpRequest = (ClientHttpRequest) returnValue;
            URI clientHttpRequestURI = clientHttpRequest.getURI();
            FrameBuilder builder = ((RestOperationCollector) getCollector()).getBuilder();
            Operation op = builder.peek();
            if (op != null && op.getType().equals(RestOperationExternalResourceAnalyzer.TYPE)) {
                String origURI = op.get(OperationFields.URI, String.class);
                if (!StringUtil.isEmpty(origURI)) {
                    String resolvedURI = clientHttpRequestURI.toString();
                    if (!origURI.equals(resolvedURI)) {
                        Frame thisFrame = builder.peekFrame();
                        OperationMap resolvedMap = getResolvedMap(thisFrame);
                        if (resolvedMap != null)
                            resolvedMap.put(resolvedURI, origURI);
                        op.put(OperationFields.URI, resolvedURI);
                    }
                }
            }
        }
    }

    public abstract pointcut accessPoint();

    // NOTE: we use cflowbelow in case the intercepted methods delegate to one another
    public pointcut collectionPoint()
            : accessPoint() && (!cflowbelow(accessPoint()))
            ;

    public final String getMethod() {
        return defaultMethod;
    }

    @Override
    protected Operation createOperation(JoinPoint jp) {
        String method = resolveOperationMethod(jp);
        String uri = resolveOperationURI(jp);
        String label = resolveOperationLabel(method, uri, jp);
        return OperationCollectionUtil.methodOperation(new Operation().type(RestOperationExternalResourceAnalyzer.TYPE), jp)
                .label(label)
                .put("method", method)
                .put(OperationFields.URI, uri)
                ;
    }

    protected String resolveOperationLabel(String method, String uri, JoinPoint jp) {
        return createLabel(method, uri);
    }

    protected String resolveOperationMethod(JoinPoint jp) {
        return getMethod();
    }

    static String resolveOperationURI(JoinPoint jp) {
        Object[] args = jp.getArgs();
        Object uri = args[0];    // all RestOperations calls have URI as 1st argument
        if (uri instanceof String) {
            return (String) uri;
        } else if (uri instanceof URI) {
            return ((URI) uri).toString();
        } else if (uri instanceof URL) {
            return ((URL) uri).toExternalForm();
        } else {
            return UNKNOWN_URI;
        }
    }

    static String createLabel(String method, String url) {
        return new StringBuilder(StringUtil.getSafeLength(method) + 1 + StringUtil.getSafeLength(url))
                .append(method.toUpperCase())
                .append(' ')
                .append(url)
                .toString();
    }

    private OperationMap getResolvedMap(Frame thisFrame) {

        Operation operation = SpringWebHelpers.getRootFrameOperation(thisFrame);
        if (operation != null) {
            OperationMap resolved = operation.get(OperationFields.UNRESOLVED_URI, OperationMap.class);
            if (resolved == null)
                resolved = operation.createMap(OperationFields.UNRESOLVED_URI);
            return resolved;
        }
        return null;

    }


}
