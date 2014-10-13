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
package com.springsource.insight.plugin.files.tracker;

import java.io.Closeable;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.springsource.insight.intercept.plugin.CollectionSettingsRegistry;
import com.springsource.insight.plugin.files.tracker.AbstractFilesTrackerAspectSupport.CacheKey;
import com.springsource.insight.plugin.files.tracker.AbstractFilesTrackerAspectSupport.FilesCache;

/**
 * Makes sure that the {@link FilesCache} does not grow beyond its max. capacity
 */
public class FilesCacheTest extends Assert {
    public FilesCacheTest() {
        super();
    }

    @Test
    public void testFilesCacheSize() {
        final int MAX_CAPACITY = 16;
        FilesCache cache = new FilesCache(MAX_CAPACITY);

        for (int index = 0; index < 2 * MAX_CAPACITY; index++) {
            CacheKey key = CacheKey.getFileKey(Mockito.mock(Closeable.class));
            assertNull("Multiple mappings for " + key, cache.put(key, String.valueOf(index)));

            if (index < MAX_CAPACITY) {
                assertEquals("Mismatched pre-populated cache size", index + 1, cache.size());
            } else {
                assertEquals("Mismatched post-populated cache size", MAX_CAPACITY, cache.size());
                assertTrue("Last value was bumped unpexpectedly", cache.containsKey(key));
            }
        }
    }

    @Test
    public void testFilesCacheCapacityUpdateWithNumber() {
        FilesCache cache = AbstractFilesTrackerAspectSupport.filesCache;
        int oldCapacity = cache.getMaxCapacity(), newCapacity = oldCapacity + 7365;
        CollectionSettingsRegistry registry = CollectionSettingsRegistry.getInstance();
        registry.set(AbstractFilesTrackerAspectSupport.MAX_TRACKED_FILES_SETTING, Integer.valueOf(newCapacity));
        assertEquals("Mismatched capacity after update", newCapacity, cache.getMaxCapacity());
    }

    @Test
    public void testFilesCacheCapacityUpdateWithString() {
        FilesCache cache = AbstractFilesTrackerAspectSupport.filesCache;
        int oldCapacity = cache.getMaxCapacity(), newCapacity = oldCapacity + 7365;
        CollectionSettingsRegistry registry = CollectionSettingsRegistry.getInstance();
        registry.set(AbstractFilesTrackerAspectSupport.MAX_TRACKED_FILES_SETTING, String.valueOf(newCapacity));
        assertEquals("Mismatched capacity after update", newCapacity, cache.getMaxCapacity());
    }

    @Test
    public void testFilesCacheCapacityUpdateWithBadString() {
        FilesCache cache = AbstractFilesTrackerAspectSupport.filesCache;
        int capacity = cache.getMaxCapacity();
        CollectionSettingsRegistry registry = CollectionSettingsRegistry.getInstance();
        registry.set(AbstractFilesTrackerAspectSupport.MAX_TRACKED_FILES_SETTING, "lyor");
        assertEquals("Unexpected capacity update success", capacity, cache.getMaxCapacity());
    }

    @Test
    public void testFilesCacheCapacityUpdateFailOnNegativeNumber() {
        FilesCache cache = AbstractFilesTrackerAspectSupport.filesCache;
        int oldCapacity = cache.getMaxCapacity(), newCapacity = 0 - oldCapacity;
        CollectionSettingsRegistry registry = CollectionSettingsRegistry.getInstance();
        registry.set(AbstractFilesTrackerAspectSupport.MAX_TRACKED_FILES_SETTING, Integer.valueOf(newCapacity));
        assertEquals("Unexpected capacity update success", oldCapacity, cache.getMaxCapacity());
    }

    @Test
    public void testFilesCacheCapacityUpdateFailOnBadValueType() {
        FilesCache cache = AbstractFilesTrackerAspectSupport.filesCache;
        int oldCapacity = cache.getMaxCapacity(), newCapacity = oldCapacity + 3777;
        CollectionSettingsRegistry registry = CollectionSettingsRegistry.getInstance();
        registry.set(AbstractFilesTrackerAspectSupport.MAX_TRACKED_FILES_SETTING, new StringBuilder().append(newCapacity));
        assertEquals("Unexpected capacity update success", oldCapacity, cache.getMaxCapacity());
    }
}
