/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
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
package com.springsource.insight.plugin.ehcache;

import java.io.Serializable;

import net.sf.ehcache.Element;

import org.junit.Test;

/**
 *
 */
public class EhcacheGetOperationCollectionAspectTest
        extends EhcacheOperationCollectionAspectTestSupport {
    public EhcacheGetOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testGetSerializable() {
        Serializable key = Long.valueOf(System.nanoTime());
        assertNull("Unexpected cache content", cache.get(key));
        assertEhcacheOperationContents(EhcacheDefinitions.GET_METHOD, key, null);
    }

    @Test
    public void testGetObject() {
        Object key = Long.valueOf(System.nanoTime());
        assertNull("Unexpected cache content", cache.get(key));
        assertEhcacheOperationContents(EhcacheDefinitions.GET_METHOD, key, null);
    }

    @Test
    public void testGetQuietSerializable() {
        Serializable key = Long.valueOf(System.nanoTime());
        assertNull("Unexpected cache content", cache.getQuiet(key));
        assertEhcacheOperationContents(EhcacheDefinitions.GET_METHOD, key, null);
    }

    @Test
    public void testGetQuietObject() {
        Object key = Long.valueOf(System.nanoTime());
        assertNull("Unexpected cache content", cache.getQuiet(key));
        assertEhcacheOperationContents(EhcacheDefinitions.GET_METHOD, key, null);
    }

    @Test
    public void testGetExistingValue() {
        Object key = "testGetExistingValue", value = Long.valueOf(System.nanoTime());
        Element putElem = putUncaptured(key, value), getElem = cache.get(key);

        assertNotNull("Missing uncaptured put", putElem);
        assertNotNull("Cached value not found", getElem);
        assertEquals("Mismatched cache content", value, getElem.getObjectValue());

        assertEhcacheOperationContents(EhcacheDefinitions.GET_METHOD, key, value);
    }

    @Override
    public EhcacheGetOperationCollectionAspect getAspect() {
        return EhcacheGetOperationCollectionAspect.aspectOf();
    }

}
