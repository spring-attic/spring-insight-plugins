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
package com.springsource.insight.plugin.files.tracker;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 */
public class FileOutputCollectionAspectTest extends FilesOpenTrackerAspectTestSupport {
    protected static File TEST_FILE;
    public FileOutputCollectionAspectTest() {
        super();
    }

    @BeforeClass
    public static void setupInputTestFile () throws Exception {
        Assert.assertNull("Test file already initialized", TEST_FILE);

        String  tmpDir=System.getProperty("java.io.tmpdir");
        Assert.assertTrue("Missing TEMP folder location value", (tmpDir != null) && (tmpDir.length() > 0));

        TEST_FILE = new File(new File(tmpDir), "output-test-file.txt");
    }

    @Test
    public void testFileOutputStreamWithFile () throws IOException {
        assertFileTrackingOperation(new FileOutputStream(TEST_FILE), TEST_FILE,
                                    "open", FileOutputCollectionAspect.resolveWriteMode(false));
    }

    @Test
    public void testFileOutputStreamWithPath () throws IOException {
        assertFileTrackingOperation(new FileOutputStream(TEST_FILE.getAbsolutePath()), TEST_FILE.getAbsolutePath(),
                                    "open", FileOutputCollectionAspect.resolveWriteMode(false));
    }

    @Test
    public void testFileOutputStreamAppendWithFile () throws IOException {
        assertFileTrackingOperation(new FileOutputStream(TEST_FILE, true), TEST_FILE,
                                    "open", FileOutputCollectionAspect.resolveWriteMode(true));
    }

    @Test
    public void testFileOutputStreamAppendWithPath () throws IOException {
        assertFileTrackingOperation(new FileOutputStream(TEST_FILE.getAbsolutePath(), true), TEST_FILE.getAbsolutePath(),
                                    "open", FileOutputCollectionAspect.resolveWriteMode(true));
    }

    @Test
    public void testFileWriterWithFile () throws IOException {
        assertFileTrackingOperation(new FileWriter(TEST_FILE), TEST_FILE,
                                    "open", FileOutputCollectionAspect.resolveWriteMode(false));
    }

    @Test
    public void testFileWriterWithPath () throws IOException {
        assertFileTrackingOperation(new FileWriter(TEST_FILE.getAbsolutePath()), TEST_FILE.getAbsolutePath(),
                                    "open", FileOutputCollectionAspect.resolveWriteMode(false));
    }

    @Test
    public void testFileWriterAppendWithFile () throws IOException {
        assertFileTrackingOperation(new FileWriter(TEST_FILE, true), TEST_FILE,
                                    "open", FileOutputCollectionAspect.resolveWriteMode(true));
    }

    @Test
    public void testFileWriterAppendWithPath () throws IOException {
        assertFileTrackingOperation(new FileWriter(TEST_FILE.getAbsolutePath(), true), TEST_FILE.getAbsolutePath(),
                                    "open", FileOutputCollectionAspect.resolveWriteMode(true));
    }

    @Test
    public void testPrintStreamWithFile () throws IOException {
        assertFileTrackingOperation(new PrintStream(TEST_FILE), TEST_FILE,
                                    "open", FileOutputCollectionAspect.resolveWriteMode(false));
    }

    @Test
    public void testPrintStreamWithPath () throws IOException {
        assertFileTrackingOperation(new PrintStream(TEST_FILE.getAbsolutePath()), TEST_FILE.getAbsolutePath(),
                                    "open", FileOutputCollectionAspect.resolveWriteMode(false));
    }

    @Test
    public void testCharsetPrintStreamWithFile () throws IOException {
        assertFileTrackingOperation(new PrintStream(TEST_FILE, "utf-8"), TEST_FILE,
                                    "open", FileOutputCollectionAspect.resolveWriteMode(false));
    }

    @Test
    public void testCharsetPrintStreamWithPath () throws IOException {
        assertFileTrackingOperation(new PrintStream(TEST_FILE.getAbsolutePath(), "utf-8"), TEST_FILE.getAbsolutePath(),
                                    "open", FileOutputCollectionAspect.resolveWriteMode(false));
    }

    @Test
    public void testPrintWriterWithFile () throws IOException {
        assertFileTrackingOperation(new PrintWriter(TEST_FILE), TEST_FILE,
                                    "open", FileOutputCollectionAspect.resolveWriteMode(false));
    }

    @Test
    public void testPrintWriterWithPath () throws IOException {
        assertFileTrackingOperation(new PrintWriter(TEST_FILE.getAbsolutePath()), TEST_FILE.getAbsolutePath(),
                                    "open", FileOutputCollectionAspect.resolveWriteMode(false));
    }

    @Test
    public void testCharsetPrintWriterWithFile () throws IOException {
        assertFileTrackingOperation(new PrintWriter(TEST_FILE, "utf-8"), TEST_FILE,
                                    "open", FileOutputCollectionAspect.resolveWriteMode(false));
    }

    @Test
    public void testCharsetPrintWriterWithPath () throws IOException {
        assertFileTrackingOperation(new PrintWriter(TEST_FILE.getAbsolutePath(), "utf-8"), TEST_FILE.getAbsolutePath(),
                                    "open", FileOutputCollectionAspect.resolveWriteMode(false));
    }
    /**
     * This is not really a test but more a micro-benchmark of how the the
     * synchronization of the tracked files map affects the performance 
     */
    @Test
    public void testSynchronizedAspectPerformance () {
        runSynchronizedAspectPerformance(new FileAccessor() {
            public Closeable createInstance() throws IOException {
                return new FileOutputStream(TEST_FILE);
            }
        });
    }

    @Override
    public FileOutputCollectionAspect getAspect() {
        return FileOutputCollectionAspect.aspectOf();
    }

}
