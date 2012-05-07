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

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.OperationCollectionUtil;
import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public abstract class FileOpenTrackerAspectSupport extends AbstractFilesTrackerAspectSupport {
    protected FileOpenTrackerAspectSupport () {
        super();
    }

    protected Operation registerOpenOperation (JoinPoint.StaticPart staticPart,
                                               Closeable            instance,
                                               File                 f,
                                               String               mode) {
        Operation   op=registerOpenOperation(staticPart, instance, f.getAbsolutePath(), mode);
        if (collectExtraInformation()) {
            addExtraInformation(op, f);
        }

        return op;
    }

    protected Operation registerOpenOperation (JoinPoint.StaticPart staticPart,
                                               Closeable            instance,
                                               String               filePath,
                                               String               mode) {
        mapOpenedFile(instance, filePath, mode);
        
        return registerOperation(new Operation()
                        .type(FilesTrackerDefinitions.TYPE)
                        .label("Open " + filePath + " (mode=" + mode + ")")
                        .sourceCodeLocation(OperationCollectionUtil.getSourceCodeLocation(staticPart))
                        .put(FilesTrackerDefinitions.OPTYPE_ATTR, "open")
                        .put(FilesTrackerDefinitions.PATH_ATTR, filePath)
                        .put(FilesTrackerDefinitions.MODE_ATTR, mode)
                        );
        
    }
}
