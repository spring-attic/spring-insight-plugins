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
package com.springsource.insight.plugin.ehcache;

import java.util.Collection;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheEntry;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.writer.CacheWriter;

import org.junit.Assert;
import org.junit.BeforeClass;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public abstract class EhcacheOperationCollectionAspectTestSupport
            extends OperationCollectionAspectTestSupport {
    protected static final String   TEST_CACHE_NAME="ehcacheOperationCollector";
    protected static CacheManager   manager;
    protected static Cache  cache;

    protected EhcacheOperationCollectionAspectTestSupport() {
       super();
    }

    // neutralizes the argument captor
    protected Element putUncaptured (Object key, Object value) {
        Assert.assertNotNull("Null key", key);
        Assert.assertNotNull("Null value", value);

        Element                             elem=new Element(key, value);
        OperationCollectionAspectSupport    collAspect=getAspect();
        OperationCollector                  current=collAspect.getCollector();
        try {
            collAspect.setCollector(IgnoringOperationCollector.DEFAULT);
            cache.put(elem);
            return elem;
        } finally {
            collAspect.setCollector(current);
        }

    }

    protected Operation assertEhcacheOperationContents (String method, Object key, Object value) {
        Operation op=getLastEntered();
        Assert.assertNotNull("No operation extracted", op);
        Assert.assertEquals("Mismatched operation type", EhcacheDefinitions.CACHE_OPERATION, op.getType());
        Assert.assertEquals("Mismatched cache name", TEST_CACHE_NAME,op.get(EhcacheDefinitions.NAME_ATTRIBUTE, String.class));
        Assert.assertEquals("Mismatched method", method, op.get(EhcacheDefinitions.METHOD_ATTRIBUTE, String.class));
        Assert.assertEquals("Mismatched key", key.toString(), op.get(EhcacheDefinitions.KEY_ATTRIBUTE));

        if (value != null) {
            Assert.assertEquals("Mismatched value", value.toString(), op.get(EhcacheDefinitions.VALUE_ATTRIBUTE));
        }
        return op;
    }

    @BeforeClass
    public static final synchronized void initTestCache () {
        if (manager != null) {
            return;
        }

        manager = CacheManager.create(EhcacheOperationCollectionAspectTestSupport.class.getResource("/ehcache.xml"));
        cache = manager.getCache(TEST_CACHE_NAME);
        Assert.assertNotNull("Test cache not found", cache);
        cache.registerCacheWriter(new TestCacheWriter(cache));
    }

    @SuppressWarnings("hiding")
    public static class TestCacheWriter implements CacheWriter, Cloneable {
        private Ehcache   cache;
        public TestCacheWriter (Ehcache cache) {
            this.cache = cache;
        }

        public CacheWriter clone(Ehcache cache) throws CloneNotSupportedException {
            TestCacheWriter writer=getClass().cast(super.clone());
            writer.cache = cache;
            return writer;
        }

        public void init() {
            System.out.println(toString() + " - initialized");
        }

        public void dispose() throws CacheException {
            System.out.println(toString() + " - disposed");
        }

        public void write(Element element) throws CacheException {
            if (element == null) {
                throw new CacheException("No element to write");
            }

            System.out.println(toString() + " write[" + element.getObjectKey() + "]=" + element.getObjectValue());
        }

        public void writeAll(Collection<Element> elements) throws CacheException {
            if ((elements == null) || elements.isEmpty()) {
                return;
            }
 
            for (Element elem : elements)
                write(elem);
        }

        public void delete(CacheEntry entry) throws CacheException {
            if (entry == null) {
                throw new CacheException("No entry to delete");
            }
            System.out.println(toString() + " - delete[" + entry.getKey() + "]");
        }

        public void deleteAll(Collection<CacheEntry> entries) throws CacheException {
            if ((entries == null) || entries.isEmpty()) {
                return;
            }
     
            for (CacheEntry e : entries) {
                delete(e);
            }
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + cache.getName() + "]";
        }
    }
}
