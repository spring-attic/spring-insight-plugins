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
package com.springsource.insight.plugin.ehcache;

import java.io.Serializable;

import net.sf.ehcache.Element;

import org.junit.Test;


/**
 *
 */
public class EhcacheRemoveOperationCollectionAspectTest
        extends EhcacheOperationCollectionAspectTestSupport {

    public EhcacheRemoveOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testRemoveObject() {
        Object key = "testRemoveObject", value = Long.valueOf(System.nanoTime());
        putUncaptured(key, value);
        assertTrue("Failed to remove element", cache.remove(key));
        assertEhcacheOperationContents(EhcacheDefinitions.REM_METHOD, key, null);
    }

    @Test
    public void testRemoveSerializable() {
        Serializable key = "testRemoveSerializable", value = Long.valueOf(System.nanoTime());
        putUncaptured(key, value);
        assertTrue("Failed to remove element", cache.remove(key));
        assertEhcacheOperationContents(EhcacheDefinitions.REM_METHOD, key, null);
    }

    @Test
    public void testRemoveQuietObject() {
        Object key = "testRemoveQuietObject", value = Long.valueOf(System.nanoTime());
        putUncaptured(key, value);
        assertTrue("Failed to remove element", cache.removeQuiet(key));
        assertEhcacheOperationContents(EhcacheDefinitions.REM_METHOD, key, null);
    }

    @Test
    public void testRemoveQuietSerializable() {
        Serializable key = "testRemoveQuietSerializable", value = Long.valueOf(System.nanoTime());
        putUncaptured(key, value);
        assertTrue("Failed to remove element", cache.removeQuiet(key));
        assertEhcacheOperationContents(EhcacheDefinitions.REM_METHOD, key, null);
    }

    @Test
    public void testRemoveObjectNotNotifyCacheReplicators() {
        Object key = "testRemoveObjectNotNotifyCacheReplicators", value = Long.valueOf(System.nanoTime());
        putUncaptured(key, value);
        assertTrue("Failed to remove element", cache.remove(key, true));
        assertEhcacheOperationContents(EhcacheDefinitions.REM_METHOD, key, null);
    }

    @Test
    public void testRemoveSerializableNotNotifyCacheReplicators() {
        Serializable key = "testRemoveSerializableNotNotifyCacheReplicators", value = Long.valueOf(System.nanoTime());
        putUncaptured(key, value);
        assertTrue("Failed to remove element", cache.remove(key, true));
        assertEhcacheOperationContents(EhcacheDefinitions.REM_METHOD, key, null);
    }

    @Test
    public void testRemoveWithWriter() {
        Object key = "testRemoveWithWriter", value = Long.valueOf(System.nanoTime());
        putUncaptured(key, value);
        assertTrue("Failed to remove element", cache.removeWithWriter(key));
        assertEhcacheOperationContents(EhcacheDefinitions.REM_METHOD, key, null);
    }

    @Test
    public void testRemoveElement() {
        Object key = "testRemoveElement", value = Long.valueOf(System.nanoTime());
        Element elem = putUncaptured(key, value);
        assertTrue("Failed to remove element", cache.removeElement(elem));
        assertEhcacheOperationContents(EhcacheDefinitions.REM_METHOD, key, null);
    }

    @Test
    public void testRemoveNonExistingElement() {
        Object key = "testRemoveNonExistingElement";
        assertFalse("Unexpected removal success", cache.remove(key));
        assertEhcacheOperationContents(EhcacheDefinitions.REM_METHOD, key, null);
    }

    @Override
    public EhcacheRemoveOperationCollectionAspect getAspect() {
        return EhcacheRemoveOperationCollectionAspect.aspectOf();
    }

}
