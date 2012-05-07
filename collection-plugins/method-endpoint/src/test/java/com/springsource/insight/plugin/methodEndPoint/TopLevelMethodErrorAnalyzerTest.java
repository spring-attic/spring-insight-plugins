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

package com.springsource.insight.plugin.methodEndPoint;

import com.springsource.insight.collection.OperationCollectionUtil;
import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.springsource.insight.intercept.operation.OperationFields.EXCEPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TopLevelMethodErrorAnalyzerTest {
    TopLevelMethodErrorAnalyzer a;
    
    @Before
    public void setUp() {
        a = new TopLevelMethodErrorAnalyzer();
    }
    
    @Test
    public void locateErrors() {
        Operation op = OperationCollectionUtil.methodOperation(this.getClass().getName(), "file", 123,
                                                               "Class#method", "method", "method()",
                                                               "Class", new String[]{});
        op.put(EXCEPTION, "boo hiss");

        Trace trace = makeDummyTrace(op);
        List<TraceError> errors = a.locateErrors(trace);
        assertEquals(1, errors.size());
        TraceError e1 = errors.get(0);
        assertEquals("boo hiss", e1.getMessage());
    }

    @Test
    public void locateErrors_noException() {
        Operation op = new Operation();
        Trace trace = makeDummyTrace(op);
        assertTrue(a.locateErrors(trace).isEmpty());
    }
    
    private Trace makeDummyTrace(Operation op) {
        SimpleFrameBuilder builder = new SimpleFrameBuilder();
        builder.enter(op);
        Frame frame = builder.exit();
        return Trace.newInstance(ApplicationName.valueOf("app"), TraceId.valueOf("0"), frame);
    }
}
