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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test {@link ExecutorService#submit(Runnable)} API
 */
public class ExecutorServiceSubmitCollectionAspectTest
        extends ExecutionCollectionAspectTestSupport {

    public ExecutorServiceSubmitCollectionAspectTest() {
        super();
    }

    @Override
    public ExecutorServiceSubmitCollectionAspect getAspect() {
        return ExecutorServiceSubmitCollectionAspect.aspectOf();
    }

    @Test
    public void testNoArgsSubmit () throws Exception {
        runSubmitTest(null);
    }

    @Test
    public void testSubmitWithResult () throws Exception {
        runSubmitTest(Long.valueOf(System.nanoTime()));
    }

    private void runSubmitTest (Object result) throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService     executor=new ThreadPoolExecutor(5, 5, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(5));
        SignallingRunnable  runner=new SignallingRunnable("runSubmitTest(result=" + result + ")");
        Future<?>           future=(result == null) ? executor.submit(runner) : executor.submit(runner, result);
        Assert.assertNotNull("No future instance returned", future);
        assertLastExecutionOperation(runner);
        assertCurrentThreadExecution();

        if (result != null) {
            Object  actual=future.get(5L, TimeUnit.SECONDS);
            Assert.assertEquals("Mismatched future result", result, actual);
        }
    }
}
