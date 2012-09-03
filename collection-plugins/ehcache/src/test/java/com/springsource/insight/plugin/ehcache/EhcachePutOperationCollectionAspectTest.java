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

import net.sf.ehcache.Element;

import org.junit.Test;

/**
 * 
 */
public class EhcachePutOperationCollectionAspectTest
        extends EhcacheOperationCollectionAspectTestSupport {
    public EhcachePutOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testPutElement () {
        Object  key="testPutElement", value=Long.valueOf(System.nanoTime());
        cache.put(new Element(key, value));
        assertEhcacheOperationContents(EhcacheDefinitions.PUT_METHOD, key, value);
    }

    @Test
    public void testPutQuiet () {
        Object  key="testPutQuiet", value=Long.valueOf(System.nanoTime());
        cache.putQuiet(new Element(key, value));
        assertEhcacheOperationContents(EhcacheDefinitions.PUT_METHOD, key, value);
    }

    @Test
    public void testPutWithWriter () {
        Object  key="testPutWithWriter", value=Long.valueOf(System.nanoTime());
        cache.putWithWriter(new Element(key, value));
        assertEhcacheOperationContents(EhcacheDefinitions.PUT_METHOD, key, value);
    }

    @Test
    public void testPutIfAbsent () {
        Object  key="testPutIfAbsent", value=Long.valueOf(System.nanoTime());
        assertNull("Unexpected previous mapping", cache.putIfAbsent(new Element(key, value)));
        assertEhcacheOperationContents(EhcacheDefinitions.PUT_METHOD, key, value);
    }

    @Test
    public void testPutNotNotifyCacheReplicators () {
        Object  key="testPutNotNotifyCacheReplicators", value=Long.valueOf(System.nanoTime());
        cache.put(new Element(key, value), true);
        assertEhcacheOperationContents(EhcacheDefinitions.PUT_METHOD, key, value);
    }
    /*
     * @see com.springsource.insight.collection.OperationCollectionAspectTestSupport#getAspect()
     */
    @Override
    public EhcachePutOperationCollectionAspect getAspect() {
        return EhcachePutOperationCollectionAspect.aspectOf();
    }

}
