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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 
 */
public class SignallingRunnable extends TestRunnable {
    private final BlockingQueue<Thread> _threadsQueue=new LinkedBlockingQueue<Thread>();
    public SignallingRunnable(String testName) {
        super(testName);
    }

    @Override
    public void run() {
        Thread  curThread=Thread.currentThread();
        super.run();
        try {
            if (!_threadsQueue.offer(curThread, 5L, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Failed to signal end of run");
            }
        } catch(InterruptedException e) {
            throw new IllegalStateException("Interrupted");
        }
    }

    public Thread waitForThread (boolean joinIt) throws InterruptedException {
        Thread  t=_threadsQueue.poll(10L, TimeUnit.SECONDS);
        if (t == null) {
            throw new IllegalStateException("No thread returned");
        }

        if (joinIt) {
            t.join(TimeUnit.SECONDS.toMillis(5L));
            if (t.isAlive()) {
                throw new IllegalStateException("Thread still running after end signalled");
            }
        }

        return t;
    }
}
