/**
 * Copyright 2009-2011 the original author or authors.
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

import java.util.List;
import java.util.TimerTask;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.JoinPoint.StaticPart;
import org.junit.After;
import org.junit.Before;

import com.springsource.insight.collection.OperationListCollector;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.operation.SourceCodeLocation;

/**
 * 
 */
public abstract class ExecutionCollectionAspectTestSupport
        extends OperationCollectionAspectTestSupport {
    protected final OperationListCollector  TEST_COLLECTOR=new OperationListCollector();
    protected final SourceCodeLocation  TEST_LOCATION=new SourceCodeLocation(getClass().getName(), "test", (-1));
    protected final RunnableResolver        TEST_RESOLVER=new RunnableResolver() {
            public TimerTask resolveTimerTask(TimerTask task, StaticPart spawnLocation) {
                return resolveRunner(task, spawnLocation);
            }
        
            public RunnableWrapper resolveRunner(Runnable command, JoinPoint.StaticPart spawnLocation) {
                if (command instanceof RunnableWrapper) {
                    return (RunnableWrapper) command;
                } else if (spawnLocation == null) {
                    return new RunnableWrapper(TEST_COLLECTOR, command, TEST_LOCATION);
                } else {
                    return new RunnableWrapper(TEST_COLLECTOR, command, spawnLocation);
                }
            }
        };

    protected RunnableResolver  originalResolver;
    protected ExecutionCollectionAspectTestSupport() {
        super();
    }

    // NOTE !!! we do not want the spy-collector so we do not call super.setUp or super.restore

    @Override
    @Before
    public void setUp () {
        // make sure we start with a clean slate
        assertNull("Original collector not cleared", originalOperationCollector);
        List<Operation> ops=TEST_COLLECTOR.getCollectedOperations();
        assertTrue("Test collector not empty", ops.isEmpty());
        assertNull("Original resolver not cleared", originalResolver);

        ExecuteMethodCollectionAspect    aspectInstance=(ExecuteMethodCollectionAspect) getAspect();
        originalOperationCollector = aspectInstance.getCollector();
        assertNotNull("No original collector", originalOperationCollector);
        aspectInstance.setCollector(TEST_COLLECTOR);

        originalResolver = aspectInstance.getRunnableResolver();
        assertNotNull("No original resolver", originalResolver);
        aspectInstance.setRunnableResolver(TEST_RESOLVER);
    }

    @Override
    @After
    public void restore () {
        assertNotNull("Original collector not saved", originalOperationCollector);
        assertNotNull("Original resolver not saved", originalResolver);

        ExecuteMethodCollectionAspect    aspectInstance=(ExecuteMethodCollectionAspect) getAspect();
        aspectInstance.setCollector(originalOperationCollector);
        originalOperationCollector = null;

        aspectInstance.setRunnableResolver(originalResolver);
        originalResolver = null;

        List<Operation> ops=TEST_COLLECTOR.getCollectedOperations();
        if (!ops.isEmpty())
            ops.clear();
    }

    protected Operation assertCurrentThreadExecution () {
        return assertFirstExecutionOperation(RunExecDefinitions.EXEC_OP, Thread.currentThread());
    }

    protected Operation assertFirstExecutionOperation (OperationType expType, Thread thread) {
        return assertExecutionOperation(TEST_COLLECTOR.getCollectedOperations(), 0, expType, thread);
    }

    protected Operation assertLastExecutionOperation (SignallingRunnable runner)
            throws InterruptedException {
        return assertLastExecutionOperation(runner, false);
    }

    protected Operation assertLastExecutionOperation (SignallingRunnable runner, boolean joinIt)
            throws InterruptedException {
        return assertLastExecutionOperation(runner.waitForThread(joinIt));
    }

    protected Operation assertLastExecutionOperation (Thread thread) {
        return assertLastExecutionOperation(RunExecDefinitions.RUN_OP, thread);
    }

    protected Operation assertLastExecutionOperation (OperationType expType, Thread thread) {
        List<Operation> ops=TEST_COLLECTOR.getCollectedOperations();
        return assertExecutionOperation(ops, ops.size() - 1, expType, thread);
    }

    protected Operation assertExecutionOperation (List<? extends Operation> opsList, int index, OperationType expType, Thread thread) {
        assertNotNull("No operations list", opsList);
        assertTrue("Negative index", index >= 0);
        assertTrue("Insufficient operations list", index < opsList.size());
        return assertExecutionOperation(opsList.get(index), expType, thread);
    }

    protected Operation assertExecutionOperation (Operation op, OperationType expType, Thread thread) {
        assertNotNull("No thread specified", thread);
        return assertExecutionOperation(op, expType, thread.getName()); 
    }

    protected Operation assertExecutionOperation (Operation op, OperationType expType, String threadName) {
        if ((op != null) && op.isFinalizable()) {
            op.finalizeConstruction();
        }

        assertNotNull("No operation", op);
        assertEquals("Mismatched operation type", expType, op.getType());
        assertEquals("Mismatched runner thread", threadName, op.get(RunExecDefinitions.THREADNAME_ATTR, String.class));
        return op;
    }
    
    protected Thread iterateRunner (SignallingRunnable runner) throws InterruptedException {
        Thread  thread=null;                
        for (int    index=0; index < Byte.SIZE; index++) {
            thread = runner.waitForThread(false);
            System.out.println("\tIteration #" + index + ": " + thread.getName());
        }
        return thread;
    }
}
