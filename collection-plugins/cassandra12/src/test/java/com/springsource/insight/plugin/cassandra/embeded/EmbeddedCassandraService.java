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
package com.springsource.insight.plugin.cassandra.embeded;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.service.CassandraDaemon;

/**
 * An in-memory Cassandra storage service that listens to the thrift interface.
 * Useful for unit testing
 */
public class EmbeddedCassandraService implements Runnable {
    CassandraDaemon cassandraDaemon;

    public void init() throws Exception {
        prepare();

        cassandraDaemon = new CassandraDaemon();
        cassandraDaemon.init(null);
    }

    public void run() {
        cassandraDaemon.start();
    }

    public void stop() throws IOException {
        cassandraDaemon.stop();
        cleanupDataDirectories();
    }

    /**
     * Creates all data dir if they don't exist and cleans them
     *
     * @throws IOException
     */
    public void prepare() throws IOException {
        // Tell cassandra where the configuration files are. Use the test configuration file.
        System.setProperty("storage-config", "../../test/resources");

        cleanupDataDirectories();
        makeDirsIfNotExist();
    }

    /**
     * Deletes all data from cassandra data directories, including the commit log.
     *
     * @throws IOException in case of permissions error etc.
     */
    public void cleanupDataDirectories() throws IOException {
        for (String s : getDataDirs()) {
            cleanDir(s);
        }
    }

    /**
     * Creates the data directories, if they didn't exist.
     *
     * @throws IOException if directories cannot be created (permissions etc).
     */
    public void makeDirsIfNotExist() throws IOException {
        for (String s : getDataDirs()) {
            mkdir(s);
        }
    }

    /**
     * Collects all data dirs and returns a set of String paths on the file system.
     *
     * @return
     */
    private Set<String> getDataDirs() {
        Set<String> dirs = new HashSet<String>();
        for (String s : DatabaseDescriptor.getAllDataFileLocations()) {
            dirs.add(s);
        }
        dirs.add(DatabaseDescriptor.getCommitLogLocation());
        dirs.add(DatabaseDescriptor.getSavedCachesLocation());
        return dirs;
    }

    /**
     * Creates a directory
     *
     * @param dir
     * @throws IOException
     */
    private void mkdir(String dir) throws IOException {
        FileUtils.createDirectory(dir);
    }

    /**
     * Removes all directory content from file the system
     *
     * @param dir
     * @throws IOException
     */
    private void cleanDir(String dir) throws IOException {
        File dirFile = new File(dir);
        if (dirFile.exists() && dirFile.isDirectory()) {
            FileUtils.deleteRecursive(dirFile);
        }
    }
}
