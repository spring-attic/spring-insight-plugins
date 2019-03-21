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

import java.io.File;
import java.net.URL;
import java.util.Collection;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheEntry;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.writer.CacheWriter;

import org.junit.BeforeClass;

import com.springsource.insight.collection.IgnoringOperationCollector;
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.FileUtil;

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
    protected Element putUncaptured (final Object key, final Object value) {
        assertNotNull("Null key", key);
        assertNotNull("Null value", value);

        final Element                             elem=new Element(key, value);
        final OperationCollectionAspectSupport    collAspect=getAspect();
        final OperationCollector                  current=collAspect.getCollector();
        try {
            collAspect.setCollector(IgnoringOperationCollector.DEFAULT);
            cache.put(elem);
            return elem;
        } finally {
            collAspect.setCollector(current);
        }

    }

    protected Operation assertEhcacheOperationContents (final String method, final Object key, final Object value) {
        final Operation op=getLastEntered();
        assertNotNull("No operation extracted", op);
        assertEquals("Mismatched operation type", EhcacheDefinitions.CACHE_OPERATION, op.getType());
        assertEquals("Mismatched cache name", TEST_CACHE_NAME,op.get(EhcacheDefinitions.NAME_ATTRIBUTE, String.class));
        assertEquals("Mismatched method", method, op.get(EhcacheDefinitions.METHOD_ATTRIBUTE, String.class));
        assertEquals("Mismatched key", key.getClass().getSimpleName(), op.get(EhcacheDefinitions.KEY_ATTRIBUTE));

        if (value != null) {
            assertEquals("Mismatched value", value.getClass().getSimpleName(), op.get(EhcacheDefinitions.VALUE_ATTRIBUTE));
        }
        return op;
    }

    @BeforeClass
    public static final synchronized void initTestCache () {
        if (manager != null) {
            return;
        }

        final Class<?>	anchorClass=EhcacheOperationCollectionAspectTestSupport.class;
        final URL				configURL=anchorClass.getResource("/ehcache.xml");
        assertNotNull("Cannot find configuration file URL");

        final File					testDir=FileUtil.detectTargetFolder(anchorClass), testStore=new File(testDir, "ehcache-store");
        final Configuration			config=ConfigurationFactory.parseConfiguration(configURL);
        final DiskStoreConfiguration	diskStore=config.getDiskStoreConfiguration();
        diskStore.setPath(testStore.getAbsolutePath());

        manager = CacheManager.create(config);
        cache = manager.getCache(TEST_CACHE_NAME);
        assertNotNull("Test cache not found", cache);
        cache.registerCacheWriter(new TestCacheWriter(cache));
    }

    @SuppressWarnings("hiding")
    public static class TestCacheWriter implements CacheWriter, Cloneable {
        private Ehcache   cache;
        public TestCacheWriter (final Ehcache cache) {
            this.cache = cache;
        }

        public CacheWriter clone(final Ehcache cache) throws CloneNotSupportedException {
            final TestCacheWriter writer=getClass().cast(super.clone());
            writer.cache = cache;
            return writer;
        }

        public void init() {
            System.out.println(toString() + " - initialized");
        }

        public void dispose() throws CacheException {
            System.out.println(toString() + " - disposed");
        }

        public void write(final Element element) throws CacheException {
            if (element == null) {
                throw new CacheException("No element to write");
            }

            System.out.println(toString() + " write[" + element.getObjectKey() + "]=" + element.getObjectValue());
        }

        public void writeAll(final Collection<Element> elements) throws CacheException {
            if ((elements == null) || elements.isEmpty()) {
                return;
            }

            for (final Element elem : elements) {
                write(elem);
            }
        }

        public void delete(final CacheEntry entry) throws CacheException {
            if (entry == null) {
                throw new CacheException("No entry to delete");
            }
            System.out.println(toString() + " - delete[" + entry.getKey() + "]");
        }

        public void deleteAll(final Collection<CacheEntry> entries) throws CacheException {
            if ((entries == null) || entries.isEmpty()) {
                return;
            }

            for (final CacheEntry e : entries) {
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
