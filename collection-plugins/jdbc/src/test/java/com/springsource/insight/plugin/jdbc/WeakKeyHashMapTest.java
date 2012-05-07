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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;


public class WeakKeyHashMapTest {
    WeakKeyHashMap<FatObj, String> storage;
    Random r = new Random();
    
    @Before
    public void setUp() {
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
    public void weakEntriesRemovedOnGC() throws Exception {
        int numItems = 100;
        for (int i=0; i< numItems; i++) {
            FatObj fatty = new FatObj(r, 1024 * 1024);
            storage.put(fatty, "fubar");
        }

        for (int i=0; i<10; i++) {
            System.gc();
        }
        
        // Put operation forces the map to clean its refqueue
        storage.put(new FatObj(r, 1), "val");
        assertTrue(storage.size() != numItems);
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
}
