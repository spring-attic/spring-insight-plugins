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

import java.util.Date;
import java.util.Timer;

import org.junit.Test;


/**
 * 
 */
public class TimerTaskScheduleCollectionAspectTest
        extends ExecutionCollectionAspectTestSupport {
    private Timer TEST_TIMER;
    public TimerTaskScheduleCollectionAspectTest() {
        super();
    }

    
    @Override
    public void setUp() {
        assertNull("Previous timer not cleared", TEST_TIMER);
        TEST_TIMER = new Timer(getClass().getSimpleName());
        super.setUp();
    }

    @Override
    public void restore() {
        assertNotNull("No current timer", TEST_TIMER);
        TEST_TIMER.cancel();
        TEST_TIMER = null;
        super.restore();
    }

    @Test
    public void testDelayedSchedule () throws InterruptedException {
        SignallingRunnable   task=new SignallingRunnable("testDelayedSchedule");
        TEST_TIMER.schedule(task, 155L);
        assertLastExecutionOperation(task);
        assertCurrentThreadExecution();
    }

    @Test
    public void testDatedSchedule () throws InterruptedException {
        SignallingRunnable   task=new SignallingRunnable("testDatedSchedule");
        TEST_TIMER.schedule(task, new Date(System.currentTimeMillis() + 155L));
        assertLastExecutionOperation(task);
        assertCurrentThreadExecution();
    }

    @Test
    public void testPeriodicDelayedSchedule () throws InterruptedException {
        SignallingRunnable   task=new SignallingRunnable("testPeriodicDelayedSchedule");
        TEST_TIMER.schedule(task, 155L, 250L);
        runPeriodicTest(task);
    }

    @Test
    public void testPeriodicDatedSchedule () throws InterruptedException {
        SignallingRunnable   task=new SignallingRunnable("testPeriodicDatedSchedule");
        TEST_TIMER.schedule(task, new Date(System.currentTimeMillis() + 155L), 250L);
        runPeriodicTest(task);
    }

    @Test
    public void testDelayedFixedRateSchedule () throws InterruptedException {
        SignallingRunnable   task=new SignallingRunnable("testDelayedFixedRateSchedule");
        TEST_TIMER.scheduleAtFixedRate(task, 155L, 250L);
        runPeriodicTest(task);
    }

    @Test
    public void testDatedFixedRateSchedule () throws InterruptedException {
        SignallingRunnable   task=new SignallingRunnable("testDatedFixedRateSchedule");
        TEST_TIMER.scheduleAtFixedRate(task, new Date(System.currentTimeMillis() + 155L), 250L);
        runPeriodicTest(task);
    }

    private void runPeriodicTest (SignallingRunnable task) throws InterruptedException {
        Thread  thread=iterateRunner(task);
        TEST_TIMER.cancel();
        assertCurrentThreadExecution();
        assertLastExecutionOperation(thread);
    }

    @Override
    public TimerTaskScheduleCollectionAspect getAspect() {
        return TimerTaskScheduleCollectionAspect.aspectOf();
    }

}
