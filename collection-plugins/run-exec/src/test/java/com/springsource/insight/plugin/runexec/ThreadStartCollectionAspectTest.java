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

import org.junit.Test;

/**
 * 
 */
public class ThreadStartCollectionAspectTest
        extends ExecutionCollectionAspectTestSupport {
    private static final Runnable DUMMY_RUNNER=new Runnable() {
            public void run () { /* do nothing */ }
        };
    private static final ThreadGroup    DUMMY_GROUP=new ThreadGroup("ThreadStartCollectionAspectTest");

    public ThreadStartCollectionAspectTest() {
        super();
    }

    @Test
    public void testThreadStart () throws InterruptedException {
        SignallingRunnable  runner=new SignallingRunnable("testThreadStart");
        Thread              thread=new Thread(runner, "tThreadStartTest");
        thread.start();
        
        Thread  sigThread=runner.waitForThread(true);
        assertSame("Mismatched runner threads", thread, sigThread);
        assertLastExecutionOperation(sigThread);
    }

    @Test
    public void testRunnableReplacement () {
        assertRunnableReplacement(new Thread(DUMMY_RUNNER), DUMMY_RUNNER);
    }

    @Test
    public void testNamedRunnableReplacement () {
        assertRunnableReplacement(new Thread(DUMMY_RUNNER, "testNamedRunnableReplacement"), DUMMY_RUNNER);
    }

    @Test
    public void testGroupRunnableReplacement () {
        assertRunnableReplacement(new Thread(DUMMY_GROUP, DUMMY_RUNNER), DUMMY_RUNNER);
    }

    @Test
    public void testGroupNamedRunnableReplacement () {
        assertRunnableReplacement(new Thread(DUMMY_GROUP, DUMMY_RUNNER, "testGroupNamedRunnableReplacement"), DUMMY_RUNNER);
    }

    @Test
    public void testGroupNamedRunnableReplacementWithStackSize () {
        assertRunnableReplacement(new Thread(DUMMY_GROUP, DUMMY_RUNNER, "testGroupNamedRunnableReplacementWithStackSize", 4 * Short.MAX_VALUE), DUMMY_RUNNER);
    }

    private void assertRunnableReplacement (Thread thread, Runnable runner) {
        ThreadStartCollectionAspect aspectInstance=getAspect();
        Runnable                    threadTarget=aspectInstance.extractThreadTarget(thread);
        assertTrue("Thread target not replaced", threadTarget instanceof RunnableWrapper);

        Runnable    command=((RunnableWrapper) threadTarget).getRunner();
        assertSame("Mismatched command instance", runner, command);
    }

    @Override
    public ThreadStartCollectionAspect getAspect() {
        return ThreadStartCollectionAspect.aspectOf();
    }
}
