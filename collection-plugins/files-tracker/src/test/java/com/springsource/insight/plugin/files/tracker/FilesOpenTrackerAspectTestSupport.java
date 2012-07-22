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
import java.io.IOException;

import org.junit.Assert;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.ClassUtil;


/**
 * 
 */
public abstract class FilesOpenTrackerAspectTestSupport extends FilesTrackerAspectTestSupport {
    protected FilesOpenTrackerAspectTestSupport() {
        super();
    }

    protected static final File resolveTestDirRoot (Class<?> anchorClass) {
		File	anchorFile=ClassUtil.getClassContainerLocationFile(anchorClass);
	    for (File classPath=anchorFile; classPath != null; classPath = classPath.getParentFile()) {
	        if ("target".equals(classPath.getName()) && classPath.isDirectory()) {
	        	return classPath;
	        }
	    }

        throw new IllegalStateException("No target folder for " + anchorClass.getSimpleName() + " at " + anchorFile);
    }

    @Override
    protected Operation assertFileTrackingOperation (Closeable instance, String filePath, String opcode, String mode) throws IOException {
        Operation   op=super.assertFileTrackingOperation(instance, filePath, opcode, mode);
        String      expected=FileOpenTrackerAspectSupport.createOperationLabel(opcode, mode, filePath);
        Assert.assertEquals("Mismatched operation label", expected, op.getLabel());
        return op;
    }
}
