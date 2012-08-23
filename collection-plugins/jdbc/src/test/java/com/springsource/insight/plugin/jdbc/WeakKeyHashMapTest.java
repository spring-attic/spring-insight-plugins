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
package com.springsource.insight.plugin.jdbc;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.springsource.insight.collection.test.AbstractCollectionTestSupport;
import com.springsource.insight.util.test.MicroBenchmark;


public class WeakKeyHashMapTest extends AbstractCollectionTestSupport  {
    WeakKeyHashMap<FatObj, String> storage;
    final Random r = new Random(System.nanoTime());
    
    @Before
    @Override
    public void setUp() {
    	super.setUp();
        storage = new WeakKeyHashMap<FatObj, String>();
    }
    
    @Test
    public void canStore1Gigs() throws Exception {
        for (int i=0; i<1000; i++) {
            FatObj fatty = new FatObj(r, 1024 * 1024);
            storage.put(fatty, "fubar");
        }
    }

    @Test
    @Category(MicroBenchmark.class)
    public void weakEntriesRemovedOnGC() throws Exception {
        final int NUM_ITEMS = 100;
        for (int i=0; i< NUM_ITEMS; i++) {
            FatObj fatty = new FatObj(r, 1024 * 1024);
            storage.put(fatty, "fubar");
        }

        encourageGC();

        // Put operation forces the map to clean its refqueue
        storage.put(new FatObj(r, 1), "val");

        assertTrue("Storage size unchanged", storage.size() != NUM_ITEMS);
    }
    
    @Test
    public void testExplicitRemoval () {
        final FatObj    KEY=new FatObj(r, 1);
        storage.put(KEY, "testExplicitRemoval");
        assertEquals("Mismatched initial size", 1, storage.size());
        
        storage.remove(KEY);
        assertEquals("Test key not removed", 0, storage.size());
    }

    public static class FatObj {
        private byte[] bytes;
        
        public FatObj(Random r, int numBytes) {
            bytes = new byte[numBytes];
            r.nextBytes(bytes);
        }
    }

    private void encourageGC() {
    	System.runFinalization();
    	for (int i= 0 ; i < 20; i++) {
    		System.gc();
        }
    }
}
