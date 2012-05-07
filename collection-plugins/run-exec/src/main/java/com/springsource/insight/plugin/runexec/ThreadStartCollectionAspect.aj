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

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;

/**
 * Intercepts {@link Thread#start()} calls and interprets them as executions
 */
public aspect ThreadStartCollectionAspect extends ExecuteMethodCollectionAspect {
    private Field targetField;  // TODO check what happens if JDK version changes
    protected final Logger  logger=Logger.getLogger(getClass().getName());

    public ThreadStartCollectionAspect () {
        try {
            targetField = Thread.class.getDeclaredField("target");
            if (!targetField.isAccessible()) {
                targetField.setAccessible(true);
            }
        } catch(Exception e) {
            logger.log(Level.SEVERE,
                       "Failed (" + e.getClass().getSimpleName()
                        + " to extract thread target field: " + e.getMessage(),
                       e);
            targetField = null;
        }
    }

    /*
     * NOTE: we need to use 'call' since Thread is a core class
     */
    public pointcut collectionPoint () : call(* Thread+.start());

    // intercept all the constructors so we can wrap their Runnable targets
    @SuppressAjWarnings({"adviceDidNotMatch"})
    Thread around(Runnable runner)
        : call(Thread+.new(Runnable))
       && args(runner) {
        Runnable    effectiveRunner=resolveRunner(runner, thisJoinPointStaticPart);
        return proceed(effectiveRunner);
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    Thread around(ThreadGroup group, Runnable runner)
        : call(Thread+.new(ThreadGroup,Runnable))
       && args(group,runner) {
        Runnable    effectiveRunner=resolveRunner(runner, thisJoinPointStaticPart);
        return proceed(group, effectiveRunner);
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    Thread around(Runnable runner, String name)
        : call(Thread+.new(Runnable,String))
       && args(runner,name) {
        Runnable    effectiveRunner=resolveRunner(runner, thisJoinPointStaticPart);
        return proceed(effectiveRunner, name);
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    Thread around(ThreadGroup group, Runnable runner, String name)
        : call(Thread+.new(ThreadGroup,Runnable,String))
       && args(group,runner,name) {
        Runnable    effectiveRunner=resolveRunner(runner, thisJoinPointStaticPart);
        return proceed(group, effectiveRunner, name);
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    Thread around(ThreadGroup group, Runnable runner, String name, long stackSize)
        : call(Thread+.new(ThreadGroup,Runnable,String,long))
       && args(group,runner,name,stackSize) {
        Runnable    effectiveRunner=resolveRunner(runner, thisJoinPointStaticPart);
        return proceed(group, effectiveRunner, name, stackSize);
    }

    @Override
    protected Runnable resolveRunnerArgument(JoinPoint jp) {
        return extractThreadTarget((Thread) jp.getTarget());
    }

    Runnable extractThreadTarget (Thread thread) {
        try {
            return (Runnable) targetField.get(thread);
        } catch(Exception e) {
            logger.log(Level.SEVERE,
                        "Failed (" + e.getClass().getSimpleName()
                         + " to extract thread target value: " + e.getMessage(),
                        e);
            return null;
        }
    }
}
