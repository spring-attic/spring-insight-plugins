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

import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

/**
 * 
 */
public abstract aspect ExecuteMethodCollectionAspect
            extends MethodOperationCollectionAspect
            implements RunnableResolver {
    private RunnableResolver    resolver=DefaultRunnableResolver.DEFAULT;

    protected ExecuteMethodCollectionAspect() {
        super();
    }

    protected ExecuteMethodCollectionAspect(OperationCollector collector) {
        super(collector);
    }
    

    @Override
    protected Operation createOperation(JoinPoint jp) {
        return createOperation(jp, RunExecDefinitions.EXEC_OP, resolveRunnerArgument(jp));
    }

    protected Operation createOperation (JoinPoint jp, OperationType type, Runnable runner) {
        Operation   op=super.createOperation(jp)
                        .type(type)
                        .put(RunExecDefinitions.THREADNAME_ATTR, Thread.currentThread().getName())
                        ;
        if (runner instanceof RunnableWrapper) {
            RunnableWrapper wrapper=(RunnableWrapper) runner;
            wrapper.setSpawnLocation(op);
            op.put(RunExecDefinitions.RUNNERID_ATTR, wrapper.getRunnerId());
        } else if (runner != null) {
            RunExecDefinitions.setSpawnLocation(op, runner);
            op.put(RunExecDefinitions.RUNNERID_ATTR, RunExecDefinitions.createRunnerId(runner));
        }
    
        return op;
    }

    RunnableResolver getRunnableResolver () {
        return resolver;
    }

    void setRunnableResolver (RunnableResolver resolverInstance) {
        if ((resolver=resolverInstance) == null) {
            throw new IllegalStateException("No resolver set");
        }
    }

    public Runnable resolveRunner(Runnable runner, JoinPoint.StaticPart spawnLocation) {
        return resolver.resolveRunner(runner, spawnLocation);
    }
    
    public TimerTask resolveTimerTask(TimerTask task, StaticPart spawnLocation) {
        return resolver.resolveTimerTask(task, spawnLocation);
    }

    protected Runnable resolveRunnerArgument(JoinPoint jp) {
        Object[]    args=jp.getArgs();
        return (Runnable) args[0];
    }
    
    @Override
    public String getPluginName() {
        return RunExecPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
