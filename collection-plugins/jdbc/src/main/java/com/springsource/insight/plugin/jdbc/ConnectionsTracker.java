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
package com.springsource.insight.plugin.jdbc;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.OperationCollectionUtil;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.plugin.CollectionSettingName;
import com.springsource.insight.intercept.plugin.CollectionSettingsRegistry;
import com.springsource.insight.intercept.plugin.CollectionSettingsUpdateListener;
import com.springsource.insight.util.logging.AbstractLoggingClass;

/**
 * A rather simplistic LRU cache that tracks {@link Connection}-s created
 * by a JDBC {@link java.sql.Driver} so that we can mark the {@link Connection#close()}
 * operation with the URL that was to open it.
 */
class ConnectionsTracker extends AbstractLoggingClass implements CollectionSettingsUpdateListener {
    /**
     * Default initial LRU capacity
     */
    static final int DEFAULT_CAPACITY = 100;
    /**
     * Default logging {@link Level} for tracker
     */
    static final Level DEFAULT_LEVEL = Level.OFF;

    private volatile int maxCapacity = DEFAULT_CAPACITY;
    private volatile Level logLevel = DEFAULT_LEVEL;
    /**
     * The tracked connections {@link Map} - key={@link CacheKey}
     * (consists of the class name and identity hash) and value=the connection
     * URL used when connection was opened
     */
    private final Map<CacheKey, String> trackedMap =
            Collections.synchronizedMap(new LinkedHashMap<CacheKey, String>() {
                private static final long serialVersionUID = 1L;

                @Override
                protected boolean removeEldestEntry(Map.Entry<CacheKey, String> entry) {
                    return size() > getMaxCapacity();
                }
            });
    private static final ConnectionsTracker INSTANCE = new ConnectionsTracker();

    protected static final CollectionSettingName MAX_TRACKED_CONNECTIONS_SETTING =
            new CollectionSettingName("max.tracked.connections", "jdbc", "Controls the number of concurrently tracked connections (default=" + DEFAULT_CAPACITY + ")");
    protected static final CollectionSettingName CONNECTION_TRACKING_LOGGING_SETTING =
            new CollectionSettingName("connections.tracking.loglevel", "jdbc", "One of the java.util.logging.Level values (default=" + DEFAULT_LEVEL + ")");

    // register a collection setting update listener and register the initial defaults
    static {
        CollectionSettingsRegistry registry = CollectionSettingsRegistry.getInstance();
        registry.addListener(INSTANCE);
    }

    private ConnectionsTracker() {
        super();
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * @param conn The created {@link Connection}
     * @param op   The {@link Operation} containing the URL in its {@link OperationFields#CONNECTION_URL}
     *             attribute
     * @return The previous assigned URL to the connection - <code>null</code>
     * if none
     */
    String startTracking(Connection conn, Operation op) {
        return startTracking(conn, op.get(OperationFields.CONNECTION_URL, String.class));
    }

    /**
     * @param conn The created {@link Connection}
     * @param url  The used URL to create the connection
     * @return The previous assigned URL to the connection - <code>null</code>
     * if none
     */
    String startTracking(Connection conn, String url) {
        CacheKey key = new CacheKey(conn);
        String prev = trackedMap.put(key, (url == null) ? "" : url);
        if ((logLevel != null) && (!Level.OFF.equals(logLevel)) && _logger.isLoggable(logLevel)) {
            _logger.log(logLevel, "startTracking(" + key + ")[" + url + "] => " + prev);
        }
        return prev;
    }

    /**
     * @param conn The {@link Connection}
     * @return The URL used when {@link #startTracking(Connection, String)}
     * was called - <code>null</code> if connection not tracked
     */
    String stopTracking(Connection conn) {
        CacheKey key = new CacheKey(conn);
        String url = trackedMap.remove(key);
        if ((logLevel != null) && (!Level.OFF.equals(logLevel)) && _logger.isLoggable(logLevel)) {
            _logger.log(logLevel, "stopTracking(" + key + ") => " + url);
        }
        return url;
    }

    /**
     * Checks if a {@link Connection} is currently being tracked
     *
     * @param conn The connection instance
     * @return The URL of the tracked connection - <code>null</code> if not tracked
     */
    String checkTrackingState(Connection conn) {
        return trackedMap.get(new CacheKey(conn));
    }

    Set<String> getTrackedURLs() {
        if (trackedMap.isEmpty()) {
            return Collections.emptySet();
        }
        return new TreeSet<String>(trackedMap.values());
    }

    int getNumTrackedConnections() {
        return trackedMap.size();
    }

    /**
     * @return A {@link Map} where key=URL, value=a {@link Collection} of all
     * the {@link CacheKey}-s currently tracking this URL
     */
    Map<String, Collection<CacheKey>> getTrackedConnections() {
        if (trackedMap.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Collection<CacheKey>> result = new TreeMap<String, Collection<CacheKey>>();
        synchronized (trackedMap) {
            for (Map.Entry<CacheKey, String> ce : trackedMap.entrySet()) {
                CacheKey key = ce.getKey();
                String url = ce.getValue();
                Collection<CacheKey> keyList = result.get(url);
                if (keyList == null) {
                    keyList = new TreeSet<CacheKey>();
                    result.put(url, keyList);
                }

                keyList.add(key);
            }
        }

        return result;
    }

    void clear() {
        trackedMap.clear();
    }

    public void incrementalUpdate(CollectionSettingName name, Serializable value) {
        if (MAX_TRACKED_CONNECTIONS_SETTING.equals(name)) {
            int newCapacity = CollectionSettingsRegistry.getIntegerSettingValue(value);
            if (newCapacity <= 0) {
                throw new IllegalArgumentException("Non-positive capacity N/A: " + value);
            }

            int oldCapacity = maxCapacity;
            maxCapacity = newCapacity;
            _logger.info("incrementalUpdate(" + name + ") " + oldCapacity + " => " + maxCapacity);
        } else if (CONNECTION_TRACKING_LOGGING_SETTING.equals(name)) {
            Level oldLevel = logLevel;
            logLevel = CollectionSettingsRegistry.getLogLevelSetting(value);
            _logger.info("incrementalUpdate(" + name + ") " + oldLevel + " => " + logLevel);
        } else if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("incrementalUpdate(" + name + ")[" + value + "] ignored");
        }
    }

    static Operation createOperation(JoinPoint jp, String url, String action) {
        return createOperation(jp.getStaticPart(), url, action);
    }

    static Operation createOperation(JoinPoint.StaticPart staticPart, String url, String action) {
        return new Operation()
                .type(JdbcDriverExternalResourceAnalyzer.TYPE)
                .sourceCodeLocation(OperationCollectionUtil.getSourceCodeLocation(staticPart))
                .label("JDBC connection " + action)
                .put(OperationFields.METHOD_NAME, action)
                .put(OperationFields.CONNECTION_URL, (url == null) ? "" : url)
                ;
    }

    static ConnectionsTracker getInstance() {
        return INSTANCE;
    }

    static class CacheKey implements Serializable, Comparable<CacheKey> {
        private static final long serialVersionUID = -470721146773085523L;
        private final String name;
        private final int hashValue;

        CacheKey(Connection conn) {
            if (conn == null) {
                throw new IllegalStateException("No connection");
            }

            name = conn.getClass().getName();
            hashValue = System.identityHashCode(conn);
        }

        public int compareTo(CacheKey o) {
            if (o == null) {
                return (-1);
            }

            if (o == this) {
                return 0;
            }

            int nRes = name.compareTo(o.name);
            if (nRes != 0) {
                return nRes;
            }

            if ((nRes = hashValue - o.hashValue) != 0) {
                return nRes;
            }

            return 0;
        }

        @Override
        public int hashCode() {
            return hashValue;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }

            CacheKey other = (CacheKey) obj;
            if (name.equals(other.name) && (hashValue == other.hashValue)) {
                return true;
            }

            return false;
        }

        @Override
        public String toString() {
            return name + "@" + hashValue;
        }
    }
}
