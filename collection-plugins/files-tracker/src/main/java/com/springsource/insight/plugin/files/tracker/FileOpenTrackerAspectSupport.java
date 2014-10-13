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
import java.io.File;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;

/**
 *
 */
public abstract class FileOpenTrackerAspectSupport extends AbstractFilesTrackerAspectSupport {
    protected FileOpenTrackerAspectSupport() {
        super();
    }

    protected Operation registerOpenOperation(JoinPoint.StaticPart staticPart,
                                              Closeable instance,
                                              File f,
                                              String mode) {
        Operation op = registerOpenOperation(staticPart, instance, f.getAbsolutePath(), mode);
        if (collectExtraInformation()) {
            addExtraInformation(op, f);
        }

        return op;
    }

    protected Operation registerOpenOperation(JoinPoint.StaticPart staticPart,
                                              Closeable instance,
                                              String filePath,
                                              String mode) {
        mapOpenedFile(instance, filePath, mode);

        return registerOperation(createOpenOperation(staticPart, filePath, mode));
    }

    Operation createOpenOperation(JoinPoint.StaticPart staticPart, String filePath, String mode) {
        return createOperation(staticPart, FilesTrackerDefinitions.OPEN_OP, filePath)
                .put(FilesTrackerDefinitions.MODE_ATTR, mode)
                ;
    }

    @Override
    protected String createOperationLabel(Operation op) {
        return super.createOperationLabel(op)
                + " (mode=" + op.get(FilesTrackerDefinitions.MODE_ATTR, String.class) + ")"
                ;
    }

    static final String createOperationLabel(String action, String mode, String path) {
        return createOperationLabel(action, path) + " (mode=" + mode + ")";
    }
}
