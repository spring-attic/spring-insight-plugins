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
package com.springsource.insight.plugin.files.tracker;

import java.io.Closeable;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.StringUtil;

/**
 * 
 */
public privileged aspect FileCloseCollectionAspect extends AbstractFilesTrackerAspectSupport {
    public FileCloseCollectionAspect () {
        super();
    }
    /*
     * NOTE: we cannot define the aspect on the execution since these may be
     * core classes, so we cannot instrument them - only the classes that
     * call them (as long as these classes are not core classes themselves)
     */

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after (Closeable c) returning
        :  call(void Closeable+.close())
        && target(c)
        && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
    {
        registerCloseOperation(thisJoinPointStaticPart, c);
    }
    
    Operation registerCloseOperation (JoinPoint.StaticPart staticPart, Closeable instance) {
        String  filePath=unmapClosedFile(instance);
        if (StringUtil.isEmpty(filePath)) {
            return null;    // just means we did not intercept the open call...
        } else {
            return registerOperation(createCloseOperation(staticPart, filePath));
        }
    }
    
    Operation createCloseOperation (JoinPoint.StaticPart staticPart, String filePath) {
        return createOperation(staticPart, FilesTrackerDefinitions.CLOSE_OP, filePath);
    }

}
