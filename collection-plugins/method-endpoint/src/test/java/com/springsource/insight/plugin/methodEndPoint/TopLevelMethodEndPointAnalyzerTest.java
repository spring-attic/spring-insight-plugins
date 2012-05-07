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
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TopLevelMethodEndPointAnalyzerTest {
    private ApplicationName app = ApplicationName.valueOf("app");    
    private TopLevelMethodEndPointAnalyzer endPointAnalyzer;
    private Operation methodOp;
    
    @Before
    public void setUp() {
        endPointAnalyzer = new TopLevelMethodEndPointAnalyzer();
        methodOp = OperationCollectionUtil.methodOperation(MyClass.class.getName(), "MyClass.file", 123,
                                                           "MyClass#myMethod", "myMethod", "myMethod(String, Object)",
                                                           "MyClass", new String[] { "arg1", "java.lang.Object" });
    }

    private static class MyClass {
        // nothing
    }

    private static class OtherClass {
        // nothing
    }

    @Test
    public void locateEndPoint() throws Exception {
        FrameBuilder b = new SimpleFrameBuilder();
        b.enter(methodOp);
        b.enter(new Operation());
        Operation other = OperationCollectionUtil.methodOperation(OtherClass.class.getName(), "OtherClass.file", 123,
                                                                  "OtherClass#otherMethod", "otherMethod", "otherMethod()",
                                                                  "OtherClass", new String[] { });

        b.enter(other);
        Thread.sleep(100);
        Frame nestedMethodFrame = b.exit();        
        Frame simpleFrame = b.exit();
        Frame methodFrame = b.exit();
        Trace trace = Trace.newInstance(app, TraceId.valueOf("0"), methodFrame);
        EndPointAnalysis endPoint = endPointAnalyzer.locateEndPoint(trace); 
        assertEquals(EndPointName.valueOf(methodOp.getSourceCodeLocation().getClassName() + "#" + methodOp.get(OperationFields.METHOD_SIGNATURE)), endPoint.getEndPointName());
        assertEquals("MyClass#myMethod", endPoint.getResourceLabel());        
    }
    
    @Test
    public void locateEndPoint_methodOperationNotTopLevel() {
        FrameBuilder b = new SimpleFrameBuilder();
        Operation httpOp = new Operation().type(OperationType.HTTP);
        b.enter(httpOp);
        b.enter(methodOp);
        Frame annotatedFrame = b.exit();        
        Frame httpFrame = b.exit();
        Trace trace = Trace.newInstance(app, TraceId.valueOf("0"), httpFrame);
        assertNull(endPointAnalyzer.locateEndPoint(trace)); 
    }
}
