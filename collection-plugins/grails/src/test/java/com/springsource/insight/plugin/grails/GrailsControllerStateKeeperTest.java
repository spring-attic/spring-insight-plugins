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

package com.springsource.insight.plugin.grails;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class GrailsControllerStateKeeperTest extends Assert {
    protected final Logger    logger=LoggerFactory.getLogger(getClass());
    public GrailsControllerStateKeeperTest() {
        super();
    }

    @Test
    public void testThreadLocalStateAccess () throws InterruptedException {
        final Semaphore             sigsem=new Semaphore(0, true);
        final Collection<Exception> excList=Collections.synchronizedCollection(new LinkedList<Exception>());
        Collection<Thread>          threadsList=new LinkedList<Thread>();
        final int                   NUM_ITERATIONS=Short.SIZE;
        for (int    index=0; index < Byte.SIZE; index++) {
            Thread  testThread=new Thread("tRunner" + index) {
                    @Override
                    public void run () {
                        Thread  t=Thread.currentThread();
                        try {
                            sigsem.acquire();

                            GrailsControllerStateKeeper.State   stateValue=null;
                            Random                              randomizer=new Random(System.nanoTime());
                            logger.info(t.getName() + " start");
                            for (int    tryIndex=0; tryIndex < NUM_ITERATIONS; tryIndex++) {
                                GrailsControllerStateKeeper.State   currentState=GrailsControllerStateKeeper.getState();
                                assertNotNull(t.getName() + " Null state value at iteration #" + tryIndex, currentState);
                                if (stateValue == null) {
                                    assertEquals(t.getName() + " Null initial value at iteration #" + tryIndex, 0, tryIndex);
                                    stateValue = currentState;
                                } else {
                                    assertSame(t.getName() + " Mismatched state instance at iteration #" + tryIndex, stateValue, currentState);
                                }
                                
                                Thread.sleep(randomizer.nextInt(Byte.MAX_VALUE) + 1L);
                            }
                            
                            GrailsControllerStateKeeper.State   remState=
                                    GrailsControllerStateKeeper.getAndDestroyThreadLocalState();
                            assertSame(t.getName() + " Mismatched removed state instance", stateValue, remState);
                            logger.info(t.getName() + " end");
                        } catch(Exception e) {
                            logger.error(t.getName() + " [" + e.getClass().getSimpleName() + "}: " + e.getMessage());
                            excList.add(e);
                        }
                    }
                };
            threadsList.add(testThread);
            testThread.start();
        }

        int numPending=threadsList.size();
        sigsem.release(numPending); // release all threads at once
        for (Thread t : threadsList) {
            t.join(numPending * TimeUnit.SECONDS.toMillis(2L) + NUM_ITERATIONS * Byte.MAX_VALUE);
            assertFalse(t.getName() + " still alive", t.isAlive());
            numPending--;
            logger.info(t.getName() + " done - pending=" + numPending);
        }

        if (!excList.isEmpty()) {
            for (Exception e : excList) {
                logger.error(e.getMessage());
            }
            
            fail("Thread exceptions captured");
        }
    }
    
    @Test
    public void testGetAndDestroyThreadLocalState() {
        assertNull("Unexpected pre-removal initial value", GrailsControllerStateKeeper.getAndDestroyThreadLocalState());
        Object  state=GrailsControllerStateKeeper.getState();
        assertNotNull("No new state created", state);
        assertSame("Mismatched removed instance", state, GrailsControllerStateKeeper.getAndDestroyThreadLocalState());
        assertNull("Unexpected new value after removal", GrailsControllerStateKeeper.getAndDestroyThreadLocalState());
    }
}
