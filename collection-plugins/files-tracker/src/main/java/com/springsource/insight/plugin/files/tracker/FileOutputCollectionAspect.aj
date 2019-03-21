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
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.PrintStream;

import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public privileged aspect FileOutputCollectionAspect extends FileOpenTrackerAspectSupport {
    public FileOutputCollectionAspect () {
        super();
    }
    /*
     * NOTE: we cannot define the aspect on the execution since these are
     * core classes, so we cannot instrument them - only the classes that
     * call them (as long as these classes are not core classes themselves)
     */

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after (File f) returning(Closeable r)
        : (call(FileOutputStream+.new(File))
        || call(FileWriter+.new(File))
        || call(PrintWriter+.new(File))
        || call(PrintStream+.new(File)))
        && args(f)
        && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
    {
        registerOpenOperation(thisJoinPointStaticPart, r, f, "w");
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after (String f) returning(Closeable r)
        : (call(FileOutputStream+.new(String))
        || call(FileWriter+.new(String))
        || call(PrintWriter+.new(String))
        || call(PrintStream+.new(String)))
        && args(f)
        && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
    {
        registerOpenOperation(thisJoinPointStaticPart, r, f, "w");
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after (File f, boolean appendMode) returning(Closeable r)
        : (call(FileOutputStream+.new(File,boolean)) || call(FileWriter+.new(File,boolean)))
        && args(f,appendMode)
        && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
    {
        registerOpenOperation(thisJoinPointStaticPart, r, f, resolveWriteMode(appendMode));
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after (String f, boolean appendMode) returning(Closeable r)
        : (call(FileOutputStream+.new(String,boolean)) || call(FileWriter+.new(String,boolean)))
        && args(f,appendMode)
        && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
    {
        registerOpenOperation(thisJoinPointStaticPart, r, f, resolveWriteMode(appendMode));
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after (File f, String csn) returning(Closeable r)
        : (call(PrintWriter+.new(File,String)) || call(PrintStream+.new(File,String)))
        && args(f,csn)
        && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
    {
        Operation   op=registerOpenOperation(thisJoinPointStaticPart, r, f, "w");
        if (op != null)
            op.putAnyNonEmpty("charset", csn);
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after (String f, String csn) returning(Closeable r)
        : (call(PrintWriter+.new(String,String)) || call(PrintStream+.new(String,String)))
        && args(f,csn)
        && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
    {
        Operation   op=registerOpenOperation(thisJoinPointStaticPart, r, f, "w");
        if (op != null)
            op.putAnyNonEmpty("charset", csn);
    }

    static final String resolveWriteMode (boolean appendMode) {
        if (appendMode) {
            return "w+";
        } else {
            return "w";
        }
    }
}
