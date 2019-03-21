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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;


/**
 * 
 */
public class SpringAsyncTaskExecutorCollectionAspectTest
        extends ExecutionCollectionAspectTestSupport {
    public SpringAsyncTaskExecutorCollectionAspectTest() {
        super();
    }

    @Test
    public void testExecutionMethod () throws InterruptedException {
        AsyncTaskExecutor   executor=new SimpleAsyncTaskExecutor("tTestExecutionMethod");
        SignallingRunnable  runner=new SignallingRunnable("testExecutionMethod");
        executor.execute(runner, 125L);

        assertLastExecutionOperation(runner);
        assertCurrentThreadExecution();
    }

    @Test
    public void testSubmissionMethod () throws InterruptedException, ExecutionException, TimeoutException {
        AsyncTaskExecutor   executor=new SimpleAsyncTaskExecutor("testSubmissionMethod");
        SignallingRunnable  runner=new SignallingRunnable("testSubmissionMethod");
        Future<?>           future=executor.submit(runner);
        Object              result=future.get(5L, TimeUnit.SECONDS);

        assertLastExecutionOperation(runner);
        assertCurrentThreadExecution();
        
        assertNull("Unexpected future result", result);
        assertTrue("Future not done", future.isDone());
    }

    @Override
    public SpringAsyncTaskExecutorCollectionAspect getAspect() {
        return SpringAsyncTaskExecutorCollectionAspect.aspectOf();
    }

}
