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

import java.sql.Connection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.springsource.insight.intercept.plugin.CollectionSettingsRegistry;

/**
 * 
 */
public class ConnectionsTrackerTest extends Assert {
    private final ConnectionsTracker  tracker=ConnectionsTracker.getInstance();
    private final CollectionSettingsRegistry registry = CollectionSettingsRegistry.getInstance();

    public ConnectionsTrackerTest() {
        super();
    }

    @Before
    public void setUp () {
        tracker.clear();    // start with a clean cache
    }

    @After
    public void restore() {
        registry.set(ConnectionsTracker.MAX_TRACKED_CONNECTIONS_SETTING,
                     Integer.valueOf(ConnectionsTracker.DEFAULT_CAPACITY));
        registry.set(ConnectionsTracker.CONNECTION_TRACKING_LOGGING_SETTING,
                     ConnectionsTracker.DEFAULT_LEVEL);
    }
    
    @Test
    public void testCachingCapacity () {
        final int       TEST_CAPACITY=Byte.SIZE;
        final String    URL="jdbc:test:testCachingCapacity";
        registry.set(ConnectionsTracker.MAX_TRACKED_CONNECTIONS_SETTING, Integer.valueOf(TEST_CAPACITY));
        assertEquals("Mismatched set capacity", TEST_CAPACITY, tracker.getMaxCapacity());
        assertEquals("Tracker not empty", 0, tracker.getNumTrackedConnections());

        for (int    index=0; index < TEST_CAPACITY; index++) {
            assertNull("Multiple tracked connections at index=" + index, tracker.startTracking(createMockConnection(), URL));
            assertEquals("Mismatched cache size", index + 1, tracker.getNumTrackedConnections());
        }

        for (int    index=0; index < TEST_CAPACITY; index++) {
            assertNull("Multiple tracked connections at index=" + (TEST_CAPACITY + index), tracker.startTracking(createMockConnection(), URL));
            assertEquals("Mismatched cache size", TEST_CAPACITY, tracker.getNumTrackedConnections());
        }
    }
    
    private static Connection createMockConnection () {
        return Mockito.mock(Connection.class);
    }
}
