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
package com.springsource.insight.plugin.runexec;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.SourceCodeLocation;

/**
 * Test {@link Executor#execute(Runnable)} API
 */
public class ExecutorExecuteCollectionAspectTest
        extends ExecutionCollectionAspectTestSupport {

    public ExecutorExecuteCollectionAspectTest() {
        super();
    }

    @Test
    public void testBasicCollection() throws InterruptedException {
        final TestExecutor executor = new TestExecutor(false, false);
        final Runnable runner = new TestRunnable("testBasicCollection");
        executor.execute(runner);

        assertFirstExecutionOperation(RunExecDefinitions.EXEC_OP, executor.waitForThread());

        final Runnable command = executor.getLastRunCommand();
        assertNotSame("Run command not wrapped", runner, command);
        assertTrue("Run command not a wrapper", command instanceof RunnableWrapper);
    }

    @Test
    public void testDirectWrappedRunner() throws InterruptedException {
        runWrappedRunnerTest(false);
    }

    @Test
    public void testAsyncWrappedRunner() throws InterruptedException {
        runWrappedRunnerTest(true);
    }

    @Test
    public void testThreadPoolExecutor() throws InterruptedException {
        Executor executor = new ThreadPoolExecutor(5, 5, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(5));
        SignallingRunnable runner = new SignallingRunnable("testThreadPoolExecutor");
        executor.execute(runner);

        {
            Operation op = assertLastExecutionOperation(runner);
            List<Operation> opsList = TEST_COLLECTOR.getCollectedOperations();
            assertEquals("Mismatched number of operations generated", 2, opsList.size());

            SourceCodeLocation scl = op.getSourceCodeLocation();
            assertEquals("Mismatched class name", SignallingRunnable.class.getName(), scl.getClassName());
            assertEquals("Mismatched method name", "run", scl.getMethodName());
        }

        {
            Operation op = assertCurrentThreadExecution();
            SourceCodeLocation scl = op.getSourceCodeLocation();
            assertEquals("Mismatched class name", getClass().getName(), scl.getClassName());
            assertEquals("Mismatched method name", "execute", scl.getMethodName());
        }
    }

    private void runWrappedRunnerTest(boolean runAsync) throws InterruptedException {
        final TestExecutor executor = new TestExecutor(true, runAsync);
        executor.execute(new TestRunnable("runWrappedRunnerTest(async=" + runAsync + ")"));

        /*
         * NOTE: last entered is the run operation since we execute using the same
         * collector
         */
        Operation op = assertLastExecutionOperation(executor.waitForThread());
        List<Operation> opsList = TEST_COLLECTOR.getCollectedOperations();
        assertEquals("Mismatched number of operations generated", 2, opsList.size());

        SourceCodeLocation scl = op.getSourceCodeLocation();
        assertEquals("Mismatched class name", TestRunnable.class.getName(), scl.getClassName());
        assertEquals("Mismatched method name", "run", scl.getMethodName());
    }

    @Override
    public ExecutorExecuteCollectionAspect getAspect() {
        return ExecutorExecuteCollectionAspect.aspectOf();
    }

}
