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
package com.springsource.insight.plugin.runexec;

import java.util.concurrent.ExecutorService;

import org.aspectj.lang.annotation.SuppressAjWarnings;

/**
 *
 */
public aspect ExecutorServiceSubmitCollectionAspect extends ExecuteMethodCollectionAspect {
    public ExecutorServiceSubmitCollectionAspect() {
        super();
    }

    /*
     * NOTE: we need to use 'call' in order to intercept calls to the Java
     *      core classes that implement this interface
     */
    public pointcut singleArgSubmit(): call(* ExecutorService+.submit(Runnable));
    public pointcut twoArgsSubmit(): call(* ExecutorService+.submit(Runnable,Object));
    public pointcut collectionPoint(): singleArgSubmit() || twoArgsSubmit();

    @SuppressAjWarnings({"adviceDidNotMatch"})
    Object around (Runnable runner)
            : singleArgSubmit() && args(runner) {
        Runnable effectiveRunner = resolveRunner(runner, thisJoinPointStaticPart);
        return proceed(effectiveRunner);
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    Object around (Runnable runner, Object argVal)
            : twoArgsSubmit() && args(runner,argVal) {
        Runnable effectiveRunner = resolveRunner(runner, thisJoinPointStaticPart);
        return proceed(effectiveRunner, argVal);
    }
}
