/**
 * Copyright 2009-2010 the original author or authors.
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
import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.collection.strategies.BasicCollectionAspectProperties;
import com.springsource.insight.collection.strategies.CollectionAspectProperties;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.plugin.CollectionSettingName;
import com.springsource.insight.intercept.plugin.CollectionSettingsRegistry;
import com.springsource.insight.intercept.plugin.CollectionSettingsUpdateListener;
import com.springsource.insight.intercept.trace.FrameBuilder;

/**
 * 
 */
public abstract class AbstractFilesTrackerAspectSupport extends OperationCollectionAspectSupport {
    private static final InterceptConfiguration configuration = InterceptConfiguration.getInstance();
    protected static final CollectionAspectProperties aspectProperties=new BasicCollectionAspectProperties(false, "files-tracker");

    public static final int DEFAULT_FILE_CACHE_SIZE=256;
    public static final boolean DEFAULT_SUPPRESS_MAPPINGS_WARNINGS_VALUE=true;
    /**
     * Default logging {@link Level} for tracker
     */
    public static final Level  DEFAULT_LEVEL=Level.OFF;

    static final FilesCache filesCache=new FilesCache(DEFAULT_FILE_CACHE_SIZE);
    /**
     * A {@link Map} of the currently open files - key=the owning instance {@link CacheKey},
     * value=the file path
     */
    static Map<CacheKey,String>   trackedFilesMap=Collections.synchronizedMap(filesCache);
    private static volatile Level  logLevel=DEFAULT_LEVEL;

    protected static final CollectionSettingName    MAX_TRACKED_FILES_SETTING =
            new CollectionSettingName("max.tracked.files", "files.tracker", "Controls the number of concurrently tracked files (default=" + DEFAULT_FILE_CACHE_SIZE + ")");
    protected static final CollectionSettingName    MAPPINGS_TRACKER_LOG_SETTING =
            new CollectionSettingName("mappings.tracker.loglevel", "files.tracker", "The java.util.logging.Level value to use for logging tracked files (default=" + DEFAULT_LEVEL + ")");

    // register a collection setting update listener and register the initial defaults
    static {
        CollectionSettingsRegistry registry = CollectionSettingsRegistry.getInstance();
        registry.addListener(new CollectionSettingsUpdateListener() {
            @SuppressWarnings("synthetic-access")
            public void incrementalUpdate (CollectionSettingName name, Serializable value) {
                Logger  LOG=Logger.getLogger(AbstractFilesTrackerAspectSupport.class.getName());
                if (MAX_TRACKED_FILES_SETTING.equals(name)) {
                    int newCapacity=CollectionSettingsRegistry.getIntegerSettingValue(value);
                    if (newCapacity <= 0) {
                        throw new IllegalArgumentException("Negative capacity N/A: " + value);
                    }
                    
                    int     prevCapacity=filesCache.updateMaxCapacity(newCapacity);
                    LOG.info("incrementalUpdate(" + name + ") " + prevCapacity + " => " + newCapacity);
                } else if (MAPPINGS_TRACKER_LOG_SETTING.equals(name)) {
                    Level newValue=CollectionSettingsRegistry.getLogLevelSetting(value);
                    LOG.info("incrementalUpdate(" + name + ") " + logLevel + " => " + newValue);
                    logLevel = newValue;
                } else if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("incrementalUpdate(" + name + ")[" + value + "] ignored");
                }
            }
        });
    }

    protected final Logger  logger=Logger.getLogger(getClass().getName()); 
    protected AbstractFilesTrackerAspectSupport () {
        super();
    }

    boolean collectExtraInformation ()
    {
        return FrameBuilder.OperationCollectionLevel.HIGH.equals(configuration.getCollectionLevel());
    }

    Operation registerOperation (Operation op) {
        if (op == null) {
            return op;
        }

        /*
         * NOTE: we generate a zero-duration frame since the purpose of this
         * plugin is to track the files. Furthermore, actually measuring the
         * duration of the open/close calls seems too complex (at least for now)
         */
        OperationCollector  collector=getCollector();
        collector.enter(op);
        collector.exitNormal();
        return op;
    }

    Operation addExtraInformation (Operation op, File f) {
        if ((op == null) || (f == null)) {
            return op;
        }

        boolean exists=f.exists();
        op.put("exists", exists);
        // files being written might not exist
        if (exists) {
            op.put("size", f.length())
              .put("lastModified", f.lastModified())
              .put("isFile", f.isFile())
              .put("isDirectory", f.isDirectory())
              .put("isAbsolute", f.isAbsolute())
              .put("isHidden", f.isHidden())
              .put("isReadable", f.canRead())
              .put("isWriteable", f.canWrite())
// only for J2SE 1.6   .put("isExecutable", f.canExecute())
              ;
        }

        return op;
    }
    /**
     * @param instance The {@link Closeable} instance created to access the file -
     * ignored if <code>null</code> 
     * @param f The accessed {@link File} - ignored if <code>null</code>
     * @param mode The file access mode
     * @return The previously tracked file path by the accessor instance -
     * <code>null</code> if no such file (which is the normal expected value)
     */
    protected String mapOpenedFile (Closeable instance, File f, String mode) {
        return mapOpenedFile(instance, (f == null) ? null : f.getAbsolutePath(), mode);
    }
    /**
     * @param instance The {@link Closeable} instance created to access the file -
     * ignored if <code>null</code> 
     * @param filePath The accessed file path - ignored if <code>null</code>/empty
     * @param mode The file access mode
     * @return The previously tracked file path by the accessor instance -
     * <code>null</code> if no such file (which is the normal expected value)
     */
    protected String mapOpenedFile (Closeable instance, String filePath, String mode) {
        if ((filePath == null) || (filePath.length() <= 0)) {
            return null;
        }

        CacheKey    k=CacheKey.getFileKey(instance);
        if (k == null) {
            return null;
        }

        String  prev=trackedFilesMap.put(k, filePath);
        if ((logLevel != null) && (!Level.OFF.equals(logLevel)) && logger.isLoggable(logLevel)) {
            logger.log(logLevel, "mapOpenedFile(" + filePath + ")[" + mode + "]@" + k + " => " + prev);
        }

        return prev;
    }
    /**
     * @param instance The {@link Closeable} instance being closed - ignored
     * if <code>null</code> 
     * @return The file path being accessed by the instance - <code>null</code>
     * if unknown file 
     */
    protected String unmapClosedFile (Closeable instance) {
        CacheKey    k=CacheKey.getFileKey(instance);
        if (k == null) {
            return null;
        }

        String    filePath=trackedFilesMap.remove(k);
        if ((logLevel != null) && (!Level.OFF.equals(logLevel)) && logger.isLoggable(logLevel)) {
            logger.log(logLevel, "unmapClosedFile(" + k + "): " + filePath);
        }

        return filePath;
    }

    /**
     * A rather simplistic LRU cache to ensure that if we miss the file close
     * of the tracked files map does not grow indefinitely. The map itself is
     * not synchronized but it is <U>wrapped</U> into one.
     */
    static final class FilesCache extends LinkedHashMap<CacheKey,String> {
        private static final long serialVersionUID = 1034264756856652293L;

        private volatile int   maxCapacity;
        public FilesCache(@SuppressWarnings("hiding") int maxCapacity) {
            super(maxCapacity);
            this.maxCapacity = maxCapacity;
        }

        public int getMaxCapacity () {
            return this.maxCapacity;
        }

        public int updateMaxCapacity (@SuppressWarnings("hiding") int maxCapacity) {
            int prev=this.maxCapacity;
            this.maxCapacity = maxCapacity;
            return prev;
        }

        @Override
        protected boolean removeEldestEntry(Entry<CacheKey, String> eldest) {
            return size() > maxCapacity;
        }
    }

    static final class CacheKey {
        private final String    name;
        private final int       value, hash;

        private CacheKey (Closeable instance)
        {
            name = instance.getClass().getName();
            value = System.identityHashCode(instance);
            // we can calculate the hash now since all values are final
            hash = name.hashCode() + value;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (this == obj) {
                return true;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            CacheKey    other=(CacheKey) obj;
            if (name.equals(other.name) && (value == other.value)) {
                return true;
            }

            return false;
        }

        @Override
        public String toString() {
            return name + "@" + Integer.toHexString(value);
        }

        static CacheKey getFileKey (Closeable instance) {
            if (instance == null)
                return null;
            else
                return new CacheKey(instance);
        }
    }

    @Override
    public String getPluginName() {
        return "files-tracker";
    }
}
