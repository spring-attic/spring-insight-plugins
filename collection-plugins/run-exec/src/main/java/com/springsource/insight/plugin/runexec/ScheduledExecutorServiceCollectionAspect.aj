/**
 * Copyright 2009-2010 the original author or authors.
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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.annotation.SuppressAjWarnings;

/**
 * 
 */
public aspect ScheduledExecutorServiceCollectionAspect extends ExecuteMethodCollectionAspect {
    public ScheduledExecutorServiceCollectionAspect() {
        super();
    }

    /*
     * NOTE: we need to use 'call' in order to intercept calls to the Java
     *      core classes that implement this interface
     */
    public pointcut scheduleRunnable () : call(* ScheduledExecutorService+.schedule(Runnable,long,TimeUnit));
    public pointcut ratedScheduling ()
        : call(* ScheduledExecutorService+.scheduleAtFixedRate(Runnable,long,long,TimeUnit))
       || call(* ScheduledExecutorService+.scheduleWithFixedDelay(Runnable,long,long,TimeUnit))
        ;
    public pointcut collectionPoint()
        : scheduleRunnable() || ratedScheduling();

    @SuppressAjWarnings({"adviceDidNotMatch"})
    Object around (Runnable runner,long delay,TimeUnit unit)
        : scheduleRunnable() && args(runner,delay,unit) {
        Runnable    effectiveRunner=resolveRunner(runner, thisJoinPointStaticPart);
        return proceed(effectiveRunner,delay,unit);
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    Object around (Runnable runner,long initialValue,long delay,TimeUnit unit)
        : ratedScheduling() && args(runner,initialValue,delay,unit) {
        Runnable    effectiveRunner=resolveRunner(runner, thisJoinPointStaticPart);
        return proceed(effectiveRunner,initialValue,delay,unit);
    }
}
