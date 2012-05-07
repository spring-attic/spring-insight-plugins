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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 */
public class ScheduledExecutorServiceCollectionAspectTest
        extends ExecutionCollectionAspectTestSupport {

    public ScheduledExecutorServiceCollectionAspectTest() {
        super();
    }

    @Test
    public void testSingleScheduling () throws InterruptedException, ExecutionException, TimeoutException {
        SignallingRunnable          runner=new SignallingRunnable("testSingleScheduling");
        ScheduledExecutorService    executor=createScheduledThreadPoolExecutor();
        ScheduledFuture<?>          future=executor.schedule(runner, 25L, TimeUnit.MILLISECONDS);
        assertLastExecutionOperation(runner);
        assertCurrentThreadExecution();

        Object   result=future.get(5L, TimeUnit.SECONDS);
        Assert.assertNull("Unexpected future execution result", result);
        Assert.assertTrue("Future not marked as done", future.isDone());
    }

    @Test
    public void testScheduleAtFixedRate () throws InterruptedException {
        runRepeatedScheduling("testScheduleAtFixedRate", true);
    }

    @Test
    public void testScheduleWithFixedDelay () throws InterruptedException {
        runRepeatedScheduling("testScheduleWithFixedDelay", false);
    }

    private void runRepeatedScheduling (String testName, boolean fixedRate) throws InterruptedException {
        SignallingRunnable          runner=new SignallingRunnable(testName);
        ScheduledExecutorService    executor=createScheduledThreadPoolExecutor();
        ScheduledFuture<?>          future=fixedRate
                    ? executor.scheduleAtFixedRate(runner, 25L, 135L, TimeUnit.MILLISECONDS)
                    : executor.scheduleWithFixedDelay(runner, 25L, 135L, TimeUnit.MILLISECONDS)
                    ;
        Thread                      thread=iterateRunner(runner);
        future.cancel(true);
        assertCurrentThreadExecution();
        assertLastExecutionOperation(thread);
    }

    @Override
    public ScheduledExecutorServiceCollectionAspect getAspect() {
        return ScheduledExecutorServiceCollectionAspect.aspectOf();
    }

    private ScheduledThreadPoolExecutor createScheduledThreadPoolExecutor () {
        return new ScheduledThreadPoolExecutor(5);
    }
}
