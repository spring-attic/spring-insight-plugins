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

import net.sf.ehcache.Element;

import org.junit.Test;


/**
 *
 */
public class EhcacheReplaceOperationCollectionAspectTest
        extends EhcacheOperationCollectionAspectTestSupport {

    public EhcacheReplaceOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testReplaceOldWithNew() {
        Object key = "testReplaceOldWithNew", oldValue = Long.valueOf(System.nanoTime());
        Element oldElem = putUncaptured(key, oldValue);
        Object newValue = Long.valueOf(System.currentTimeMillis() + System.nanoTime());
        Element newElem = new Element(key, newValue);

        assertTrue("Failed to replace", cache.replace(oldElem, newElem));
        assertEhcacheOperationContents(EhcacheDefinitions.RPL_METHOD, key, newValue);
    }

    @Test
    public void testReplaceInPlace() {
        Object key = "testReplaceInPlace", oldValue = Long.valueOf(System.nanoTime());
        Element oldElem = putUncaptured(key, oldValue);
        Object newValue = Long.valueOf(System.currentTimeMillis() + System.nanoTime());
        Element prevElem = cache.replace(new Element(key, newValue));

        assertSame("Mismatched replaced element", oldElem, prevElem);
        assertNotNull("Failed to replace", prevElem);
        assertEhcacheOperationContents(EhcacheDefinitions.RPL_METHOD, key, newValue);
    }

    @Override
    public EhcacheReplaceOperationCollectionAspect getAspect() {
        return EhcacheReplaceOperationCollectionAspect.aspectOf();
    }

}
