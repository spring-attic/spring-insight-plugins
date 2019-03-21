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

package com.springsource.insight.plugin.springweb.remoting;

import java.util.Collection;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.httpinvoker.HttpInvokerRequestExecutor;
import org.springframework.remoting.support.RemoteInvocation;

import com.springsource.insight.collection.OperationCollectionUtil;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.method.JoinPointBreakDown;
import com.springsource.insight.intercept.trace.FrameBuilder;
import com.springsource.insight.plugin.springweb.AbstractSpringWebAspectSupport;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.MapUtil;
import com.springsource.insight.util.StringUtil;

/**
 *
 */
public aspect HttpInvokerRequestExecutorOperationCollectionAspect extends AbstractSpringWebAspectSupport {
    private static final InterceptConfiguration configuration = InterceptConfiguration.getInstance();

    public HttpInvokerRequestExecutorOperationCollectionAspect() {
        super(new HttpInvokerRequestExecutorOperationCollector());
    }

    public pointcut collectionPoint()
            : execution(* HttpInvokerRequestExecutor+.executeRequest(HttpInvokerClientConfiguration,RemoteInvocation));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[] args = jp.getArgs();
        HttpInvokerClientConfiguration config = (HttpInvokerClientConfiguration) args[0];
        RemoteInvocation invocation = (RemoteInvocation) args[1];
        Operation op = OperationCollectionUtil.methodOperation(
                new Operation().type(HttpInvokerRequestExecutorExternalResourceAnalyzer.HTTP_INVOKER), jp)
                .put(HttpInvokerRequestExecutorExternalResourceAnalyzer.DIRECT_CALL_ATTR, false);

        encodeInvocationConfig(op, config);
        encodeRemoteInvocation(op, invocation);
        return op;
    }

    protected Operation encodeInvocationConfig(Operation op, HttpInvokerClientConfiguration config) {
        if (config instanceof HttpInvokerProxyFactoryBean) {
            Class<?> serviceInterface = ((HttpInvokerProxyFactoryBean) config).getServiceInterface();
            if (serviceInterface != null) {
                op.put("serviceInterface", serviceInterface.getName());
            }
        }

        op.put(OperationFields.URI, config.getServiceUrl());

        if (collectExtraInformation()) {
            encodeCodebaseUrls(op.createList("codebaseUrls"), config.getCodebaseUrl());
        }

        return op;
    }

    protected OperationList encodeCodebaseUrls(OperationList list, String urlsString) {
        Collection<String> urls = StringUtil.explode(urlsString, " ", true, true);
        if (ListUtil.size(urls) <= 0) {
            return list;
        }

        for (String url : urls) {
            list.add(url);
        }

        return list;
    }

    protected Operation encodeRemoteInvocation(Operation op, RemoteInvocation invocation) {
        String methodName = invocation.getMethodName();
        String remoteLocation = JoinPointBreakDown.getMethodStringFromArgs(methodName, invocation.getParameterTypes());

        op.label(generateLabel(op, remoteLocation)).put("remoteMethodSignature", remoteLocation);

        if (collectExtraInformation()) {
            encodeInvocationAttributes(op.createMap("remoteInvocationAttrs"), invocation.getAttributes());
        }

        return op;
    }

    protected String generateLabel(Operation op, String remoteLocation) {
        String simpleServiceInterface = null;
        String serviceInterface = op.get("serviceInterface", String.class);

        StringBuilder builder;
        if (!StringUtil.isEmpty(serviceInterface)) {
            int lastIndex = serviceInterface.lastIndexOf('.');
            simpleServiceInterface = serviceInterface.substring(lastIndex + 1, serviceInterface.length());

            builder = new StringBuilder(simpleServiceInterface.length() + remoteLocation.length() + 1);
            builder.append(simpleServiceInterface).append('#');

        } else {
            builder = new StringBuilder(remoteLocation.length());
        }

        builder.append(remoteLocation);

        return builder.toString();

    }

    protected OperationMap encodeInvocationAttributes(OperationMap map, Map<String, ?> attrs) {
        if (MapUtil.size(attrs) <= 0) {
            return map;
        }

        for (Map.Entry<String, ?> ae : attrs.entrySet()) {
            String key = ae.getKey();
            Object value = ae.getValue();
            map.putAnyNonEmpty(key, value);
        }

        return map;
    }

    boolean collectExtraInformation() {
        return FrameBuilder.OperationCollectionLevel.HIGH.equals(configuration.getCollectionLevel());
    }

    @Override
    public boolean isMetricsGenerator() {
        return true; // This provides an external resource
    }
}
