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
package com.springsource.insight.plugin.jaxrs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.util.KeyValPair;
import com.springsource.insight.util.StringFormatterUtils;

/**
 */
public aspect JaxrsOperationCollectionAspect extends MethodOperationCollectionAspect {
    public pointcut pathContainer ()
        : execution(* (@Path *).*(..));

    public pointcut deleteOperation ()
        : execution(@DELETE * *(..))
        ;

    public pointcut getOperation ()
        : execution(@GET * *(..))
        ;

    public pointcut headOperation ()
        : execution(@HEAD * *(..))
        ;
    
    public pointcut postOperation ()
        : execution(@POST * *(..))
        ;

    public pointcut putOperation ()
        : execution(@PUT * *(..))
        ;

    public pointcut requestOperation ()
        : deleteOperation()
       || getOperation()
       || headOperation()
       || postOperation()
       || putOperation()
        ;
    /*
     * @see com.springsource.insight.collection.AbstractOperationCollectionAspect#collectionPoint()
     */
    public pointcut collectionPoint ()
        : pathContainer() && requestOperation()
        ;
    /*
     * @see com.springsource.insight.collection.method.MethodOperationCollectionAspect#createOperation(org.aspectj.lang.JoinPoint)
     */
    @Override
    protected Operation createOperation(JoinPoint jp) {
        return updateOperation(jp, super.createOperation(jp).type(JaxrsDefinitions.TYPE));
    }

    @Override
    public boolean isMetricsGenerator() {
        return true;
    }

    @Override
    public String getPluginName() {
        return JaxrsPluginRuntimeDescriptor.PLUGIN_NAME;
    }

    @SuppressWarnings("unchecked")
    private static final List<Class<? extends Annotation>>  METHOD_ANNOTATIONS=Arrays.asList(
            DELETE.class, GET.class, HEAD.class, POST.class, PUT.class
        );
    protected static final Operation updateOperation (final JoinPoint jp, final Operation op) {
        final Signature sig=(jp == null) ? null : jp.getSignature();
        final Class<?>  clazz=(sig == null) ? null : sig.getDeclaringType();
        StringBuilder   sb=appendPathValue(null, (clazz == null) ? null : clazz.getAnnotation(Path.class));

        if (sig instanceof MethodSignature) {
            final Method    method=((MethodSignature) sig).getMethod();
            sb = appendPathValue(sb, (method == null) ? null : method.getAnnotation(Path.class));

            // TODO parse the annotations to build the full path as well as the current value

            final KeyValPair<Class<? extends Annotation>,Annotation>    ann=findFirstMethodAnnotation(method, METHOD_ANNOTATIONS);
            if (ann != null) {
                op.put("method", ann.getKey().getSimpleName());
            } else {
                op.put("method", "<unknown>");
            }
            
            final Map<String,KeyValPair<Class<? extends Annotation>, ?>>    pathParams=
                getPathParametersValues((method == null) ? null : method.getParameterAnnotations(), jp.getArgs());
            final Collection<? extends Map.Entry<String,KeyValPair<Class<? extends Annotation>, ?>>>    paramsList=
                ((pathParams == null) || (pathParams.size() <= 0)) ? null : pathParams.entrySet();
            final OperationList opList=op.createList("pathParams");
            if ((paramsList != null) && (paramsList.size() > 0)) {
                for (final Map.Entry<String,KeyValPair<Class<? extends Annotation>, ?>> paramEntry : paramsList) {
                    final String                                        paramName=
                        (paramEntry == null) ? null : paramEntry.getKey();
                    final KeyValPair<Class<? extends Annotation>, ?>    paramDesc=
                        (paramEntry == null) ? null : paramEntry.getValue();
                    final Class<?>                                      paramType=
                        (paramDesc == null) ? null : paramDesc.getKey();
                    final Object                                        paramValue=
                        (paramDesc == null) ? null : paramDesc.getValue();
                    if ((paramName == null) || (paramName.length() <= 0) || (paramType == null)) {
                        continue;
                    }

                    final OperationMap  pvMap=opList.createMap().put("name", paramName);
                    pvMap.putAny("value", paramValue);
                    pvMap.put("type", paramType.getSimpleName());
                }
            }
        }

        if ((sb != null) && (sb.length() > 0)) {
            op.put("requestTemplate", sb.toString());
        }

        return op;
    }

    private static final StringBuilder appendPathValue (final StringBuilder org, final Path path) {
        final String    value=(path == null) ? null : path.value();
        if ((value == null) || (value.length() <= 0)) {
            return org;
        }

        /*
         * According to Path annotation Javadoc: For the purposes of absolutizing
         * a path against the base URI , a leading '/' in a path is ignored
         */
        if ((value.charAt(0) == '/') && (value.length() <= 1)) {
            return org;
        }

        final StringBuilder sb=(org == null) ? new StringBuilder(Math.max(value.length(),32)) : org;
        final int           curLen=sb.length();
        if ((curLen > 0) && (sb.charAt(curLen - 1) != '/')) {
            sb.append('/'); // separate from previous component
        }

        /*
         * According to Path annotation Javadoc: For the purposes of absolutizing
         * a path against the base URI , a leading '/' in a path is ignored
         */
        if (value.charAt(0) == '/') {
            return sb.append(value.substring(1));
        }

        return sb.append(value);
    }

    private static final KeyValPair<Class<? extends Annotation>,Annotation> findFirstMethodAnnotation (
            final Method method, final Collection<Class<? extends Annotation>> annsList) {
        if ((method == null) || (annsList == null) || annsList.isEmpty()) {
            return null;
        }

        for (final Class<? extends Annotation> annClass : annsList) {
            final Annotation a=(annClass == null) ? null : method.getAnnotation(annClass);
            if (a != null) {
                return new KeyValPair<Class<? extends Annotation>,Annotation>(annClass, a);
            }
        }

        return null;    // no match found
    }

    private static final Map<String,KeyValPair<Class<? extends Annotation>, ?>> getPathParametersValues (
            final Annotation[][] paramAnns, final Object[] args) {
        final int   numParams=(paramAnns == null) ? 0 : paramAnns.length,
                    numArgs=(args == null) ? 0 : args.length;
        if ((numParams <= 0) || (numArgs <= 0)) {
            return Collections.emptyMap();
        }

        final Map<String,KeyValPair<Class<? extends Annotation>, ?>>    paramsMap=
            new TreeMap<String,KeyValPair<Class<? extends Annotation>, ?>>();
        for (int pIndex=0; pIndex < numParams; pIndex++) {
            final Annotation[]  anns=paramAnns[pIndex];
            if ((anns == null) || (anns.length <= 0)) {
                continue;
            }

            for (final Annotation a : anns) {
                final JaxrsParamType    paramType=JaxrsParamType.fromAnnotation(a);
                if (paramType == null) {    // simply means not an annotation of interest
                    continue;
                }

                final String    paramName=stripParameterName(paramType.getValue(a));
                if ((paramName == null) || (paramName.length() <= 0)) {
                    throw new IllegalStateException("No path param name for type=" + paramType);
                }

                final Object                                    paramValue=
                    (pIndex >= numArgs) ? null : args[pIndex];
                final KeyValPair<Class<? extends Annotation>,?> paramDesc=
                    new KeyValPair<Class<? extends Annotation>,Object>(
                            paramType.getAnnotationClass(), (paramValue == null) ? StringFormatterUtils.NULL_VALUE_STRING : paramValue);
                final KeyValPair<Class<? extends Annotation>,?> prevValue=
                    paramsMap.put(paramName, paramDesc);
                if (prevValue != null) {
                    throw new IllegalStateException("Multiple mappings for path param=" + paramName);
                }

                break;  // there can be only one path parameter annotation per parameter
            }
        }

        return paramsMap;
    }

    private static final String stripParameterName (final String paramName) {
        final int   nameLen=(paramName == null) ? 0 : paramName.length();
        if ((nameLen >= 2) && (paramName.charAt(0) == '{') && (paramName.charAt(nameLen - 1) == '}')) {
            return paramName.substring(1, nameLen - 1);
        }

        return paramName;   // TODO consider throwing a "formatException"
    }
    
}
