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

import com.springsource.insight.collection.OperationCollectionUtil;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.SourceCodeLocation;

/**
 * 
 */
class RunnableWrapper extends TimerTask {
    private final Runnable  runner;
    private final SourceCodeLocation    rootLocation, spawnLocation;
    private final String    rootLabel, runnerId;
    private OperationCollector operationCollector;

    public RunnableWrapper (OperationCollector      collector,
                            Runnable                runnable,
                            JoinPoint.StaticPart    staticPart) {
        this(collector, runnable, OperationCollectionUtil.getSourceCodeLocation(staticPart));
    }

    public RunnableWrapper (OperationCollector  collector,
                            Runnable            runnable,
                            SourceCodeLocation  forkLocation) {
        if ((operationCollector=collector) == null) {
            throw new IllegalStateException("No collector specified");
        }

        if ((runner=runnable) == null) {
            throw new IllegalStateException("No runner to wrap");
        }

        if ((spawnLocation=forkLocation) == null) {
            throw new IllegalStateException("No spawn location");
        }

        Class<?>    runnerClass=RunExecDefinitions.resolveRunnerClass(runnable);
        runnerId = RunExecDefinitions.createRunnerId(runnable, runnerClass);
        rootLocation = new SourceCodeLocation(runnerClass.getName(), "run", (-1));
        rootLabel = rootLocation.getClassName() + "#" + rootLocation.getMethodName() + "()";
    }

    public OperationCollector getCollector() {
        return operationCollector;
    }

    public void setCollector(OperationCollector collector) {
        if ((operationCollector=collector) == null) {
            throw new IllegalStateException("No collector specified");
        }
    }

    public String getRunnerId () {
        return runnerId;
    }

    public Runnable getRunner () {
        return runner;
    }

    @Override
    public void run() {
        OperationCollector  collector=getCollector();
        Operation           op=createRootOperation(Thread.currentThread());
        collector.enter(op);
        try {
            Runnable    command=getRunner();
            command.run();
            collector.exitNormal();
        } catch(RuntimeException e) {
            collector.exitAbnormal(e);
            throw e;
        }
    }

    Operation createRootOperation (Thread curThread) {
        Operation   op=new Operation()
                       .type(RunExecDefinitions.RUN_OP)
                       .label(rootLabel)
                       .sourceCodeLocation(rootLocation)
                       .put(RunExecDefinitions.THREADNAME_ATTR, curThread.getName())
                       .put(RunExecDefinitions.RUNNERID_ATTR, getRunnerId())
                       ;
        setSpawnLocation(op);
        return op;
    }
    
    OperationMap setSpawnLocation (Operation op) {
        return RunExecDefinitions.setSpawnLocation(op, spawnLocation);
    }
}
