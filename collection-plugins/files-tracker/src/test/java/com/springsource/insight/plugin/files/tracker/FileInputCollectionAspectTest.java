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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Runs junit tests on {@link FileInputCollectionAspect}
 */
public class FileInputCollectionAspectTest extends FilesTrackerAspectTestSupport {
    protected static File TEST_FILE;
    public FileInputCollectionAspectTest() {
        super();
    }

    @BeforeClass
    public static void setupInputTestFile () throws Exception {
        Assert.assertNull("Test file already initialized", TEST_FILE);
        ClassLoader cl=Thread.currentThread().getContextClassLoader();
        URL         url=cl.getResource("input-test-file.txt");
        Assert.assertNotNull("Cannot resolve input test file location", url);
        TEST_FILE = new File(url.toURI());
    }

    @Test
    public void testFileInputStreamWithFile () throws IOException {
        assertFileTrackingOperation(new FileInputStream(TEST_FILE), TEST_FILE, "open", "r");
    }

    @Test
    public void testFileInputStreamWithPath () throws IOException {
        assertFileTrackingOperation(new FileInputStream(TEST_FILE.getAbsolutePath()), TEST_FILE.getAbsolutePath(), "open", "r");
    }

    @Test
    public void testFileReaderWithFile () throws IOException {
        assertFileTrackingOperation(new FileReader(TEST_FILE), TEST_FILE, "open", "r");
    }

    @Test
    public void testFileReaderWithPath () throws IOException {
        assertFileTrackingOperation(new FileReader(TEST_FILE.getAbsolutePath()), TEST_FILE.getAbsolutePath(), "open", "r");
    }

    @Test
    public void testRandomAccessFileWithFile () throws IOException {
        assertFileTrackingOperation(new RandomAccessFile(TEST_FILE,"r"), TEST_FILE, "open", "r");
    }

    @Test
    public void testRandomAccessFileWithPath () throws IOException {
        assertFileTrackingOperation(new RandomAccessFile(TEST_FILE.getAbsolutePath(), "r"), TEST_FILE.getAbsolutePath(), "open", "r");
    }
    /**
     * This is not really a test but more a micro-benchmark of how the the
     * synchronization of the tracked files map affects the performance 
     */
    @Test
    public void testSynchronizedAspectPerformance () {
        runSynchronizedAspectPerformance(new FileAccessor() {
            public Closeable createInstance() throws IOException {
                return new FileInputStream(TEST_FILE);
            }
        });
    }

    @Override
    public FileInputCollectionAspect getAspect() {
        return FileInputCollectionAspect.aspectOf();
    }
}
