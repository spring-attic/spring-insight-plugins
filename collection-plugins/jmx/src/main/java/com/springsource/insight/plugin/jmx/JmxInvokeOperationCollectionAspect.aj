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

package com.springsource.insight.plugin.jmx;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.ArrayUtil;

/**
 *
 */
public aspect JmxInvokeOperationCollectionAspect extends JmxOperationCollectionAspectSupport {
    public JmxInvokeOperationCollectionAspect() {
        super();
    }

    // need to use 'call' since some implementations come from the core
    public pointcut methodInvocation()
            : (call(* MBeanServer+.invoke(ObjectName,String,Object[],String[])))
            || (call(* MBeanServerConnection+.invoke(ObjectName,String,Object[],String[])))
            ;

    /* We use cflowbelow in case calls are delegated - theoretically, one
     * might make a case against the cflowbelow - e.g., if the server invokes
     * some other method. However, this is considered (a) highly unlikely,
     * (b) not really useful information and (c) considerable trace size increase
     */
    public pointcut collectionPoint()
            : methodInvocation() && (!cflowbelow(methodInvocation()))
            ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[] args = jp.getArgs();
        String methodName = ArrayUtil.findFirstInstanceOf(String.class, args);
        String[] signature = ArrayUtil.findFirstInstanceOf(String[].class, args);
        Object[] paramValues = ArrayUtil.findFirstInstanceOf(Object[].class, args);
        return JmxInvocationEndPointAnalyzer.updateOperation(createBeanOperation(jp, getObjectName(args)), methodName, signature, paramValues);
    }

}
