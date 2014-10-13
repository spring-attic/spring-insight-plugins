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
package com.springsource.insight.plugin.jws;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.util.ArrayUtil;

/**
 *
 */
public aspect JwsOperationCollectionAspect extends MethodOperationCollectionAspect {
    public JwsOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint()
            : execution(* (@WebService *).*(..))
            ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        return updateOperation(jp, super.createOperation(jp).type(JwsDefinitions.TYPE));
    }

    @Override
    public boolean isMetricsGenerator() {
        return true;
    }

    @Override
    public String getPluginName() {
        return JwsPluginRuntimeDescriptor.PLUGIN_NAME;
    }

    protected static final Operation updateOperation(final JoinPoint jp, final Operation op) {
        final Signature sig = (jp == null) ? null : jp.getSignature();
        final Class<?> clazz = (sig == null) ? null : sig.getDeclaringType();
        fillServiceInformation(op, (clazz == null) ? null : clazz.getAnnotation(WebService.class));

        if (sig instanceof MethodSignature) {
            return fillMethodInformation(op, ((MethodSignature) sig).getMethod(), jp.getArgs());
        }

        return op;  // debug breakpoint
    }

    protected static final Operation fillServiceInformation(final Operation op, final WebService service) {
        if ((op == null) || (service == null)) {
            return op;
        }

        op.putAnyNonEmpty("name", service.name());
        op.putAnyNonEmpty("targetNamespace", service.targetNamespace());
        op.putAnyNonEmpty("serviceName", service.serviceName());
        op.putAnyNonEmpty("portName", service.portName());
        op.putAnyNonEmpty("wsdlLocation", service.wsdlLocation());
        op.putAnyNonEmpty("endpointInterface", service.endpointInterface());
        return op;
    }

    protected static final Operation fillMethodInformation(final Operation op, final Method method, final Object... args) {
        if ((op == null) || (method == null)) {
            return op;
        }

        fillMethodInformation(op, method.getAnnotation(WebMethod.class));
        fillMethodParamsInformation(op, method.getParameterTypes(), method.getParameterAnnotations(), args);
        return op;
    }

    protected static final Operation fillMethodInformation(final Operation op, final WebMethod method) {
        if ((op == null) || (method == null)) {
            return op;
        }

        op.putAnyNonEmpty("operationName", method.operationName());
        op.putAnyNonEmpty("action", method.action());
        return op.put("exclude", method.exclude());
    }

    protected static final Operation fillMethodParamsInformation(
            final Operation op, final Class<?>[] paramTypes, final Annotation[][] paramAnns, final Object[] args) {
        if ((op == null)
                || (ArrayUtil.length(paramTypes) <= 0)
                || (ArrayUtil.length(paramAnns) <= 0)
                || (ArrayUtil.length(args) <= 0)) {
            return op;
        }

        OperationList paramsList = op.createList("webParams");
        for (int pIndex = 0; pIndex < paramTypes.length; pIndex++) {
            final Annotation[] anns = paramAnns[pIndex];
            if ((anns == null) || (anns.length <= 0)) {
                continue;
            }

            for (final Annotation a : anns) {
                if (!(a instanceof WebParam)) {
                    continue;
                }

                final OperationMap map = paramsList.createMap();
                final WebParam wp = (WebParam) a;
                map.put("header", wp.header());
                map.putAnyNonEmpty("name", wp.name());
                map.putAnyNonEmpty("partName", wp.partName());
                map.putAnyNonEmpty("targetNamespace", wp.targetNamespace());
                map.putAnyNonEmpty("mode", wp.mode());

                break;  // there can be only one parameter annotation per parameter
            }
        }

        return op;
    }
}