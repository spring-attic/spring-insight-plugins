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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.RandomAccessFile;

import org.aspectj.lang.annotation.SuppressAjWarnings;

/**
 * 
 */
public privileged aspect FileInputCollectionAspect extends FileOpenTrackerAspectSupport {
    public FileInputCollectionAspect () {
        super();
    }

    /*
     * NOTE: we cannot define the aspect on the execution since these are
     * core classes, so we cannot instrument them - only the classes that
     * call them (as long as these classes are not core classes themselves)
     */

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after (File f) returning(Closeable r)
        : (call(FileInputStream+.new(File)) || call(FileReader+.new(File)))
       && args(f)
       && if(strategies.collect(aspectProperties, thisJoinPointStaticPart))
    {
        registerOpenOperation(thisJoinPointStaticPart, r, f, "r");
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after (String f) returning(Closeable r)
        : (call(FileInputStream+.new(String)) || call(FileReader+.new(String)))
       && args(f)
       && if(strategies.collect(aspectProperties, thisJoinPointStaticPart))
    {
        registerOpenOperation(thisJoinPointStaticPart, r, f, "r");
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after (File f, String mode) returning(Closeable r)
        : call(RandomAccessFile+.new(File,String))
       && args(f,mode)
       && if(strategies.collect(aspectProperties, thisJoinPointStaticPart))
    {
        registerOpenOperation(thisJoinPointStaticPart, r, f, mode);
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after (String f, String mode) returning(Closeable r)
        : call(RandomAccessFile+.new(String,String))
       && args(f,mode)
       && if(strategies.collect(aspectProperties, thisJoinPointStaticPart))
    {
        registerOpenOperation(thisJoinPointStaticPart, r, f, mode);
    }
}
