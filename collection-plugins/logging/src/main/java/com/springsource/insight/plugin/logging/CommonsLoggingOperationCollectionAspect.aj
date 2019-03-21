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
package com.springsource.insight.plugin.logging;

import org.apache.commons.logging.Log;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

import com.springsource.insight.intercept.operation.Operation;

/**
 *
 */
public aspect CommonsLoggingOperationCollectionAspect extends LoggingMethodOperationCollectionAspect {
    public pointcut errorLogFlow()
            : execution(* Log+.error(Object))
            || execution(* Log+.error(Object,Throwable))
            || execution(* Log+.fatal(Object))
            || execution(* Log+.fatal(Object,Throwable))
            ;

    public pointcut collectionPoint()
            : errorLogFlow()
            && (!cflowbelow(errorLogFlow()))
            ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Signature sig = jp.getSignature();
        Object[] args = jp.getArgs();
        return createOperation(jp, Log.class, sig.getName().toUpperCase(),
                String.valueOf(args[0]), (args.length > 1) ? (Throwable) args[1] : null);
    }

}
