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

import java.util.TimerTask;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.JoinPoint.StaticPart;

import com.springsource.insight.collection.DefaultOperationCollector;
import com.springsource.insight.collection.OperationCollector;

class DefaultRunnableResolver implements RunnableResolver {
    static final DefaultRunnableResolver DEFAULT = new DefaultRunnableResolver();

    public DefaultRunnableResolver() {
        super();
    }

    public RunnableWrapper resolveTimerTask(TimerTask task, StaticPart spawnLocation) {
        return resolveRunner(task, spawnLocation);
    }

    public RunnableWrapper resolveRunner(Runnable runner, JoinPoint.StaticPart spawnLocation) {
        // avoid double wrapping
        if (runner instanceof RunnableWrapper) {
            return (RunnableWrapper) runner;
        } else {
            return resolveWrapper(new DefaultOperationCollector(), runner, spawnLocation);
        }
    }

    RunnableWrapper resolveWrapper(OperationCollector collector, Runnable runner, JoinPoint.StaticPart spawnLocation) {
        if ((collector == null) || (runner == null)) {
            throw new IllegalArgumentException("No collector/runner");
        }

        // avoid double wrapping
        if (runner instanceof RunnableWrapper) {
            return (RunnableWrapper) runner;
        } else {
            return new RunnableWrapper(collector, runner, spawnLocation);
        }
    }
}