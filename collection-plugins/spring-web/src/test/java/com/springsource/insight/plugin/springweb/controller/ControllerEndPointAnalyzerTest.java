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

package com.springsource.insight.plugin.springweb.controller;

import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.SimpleFrameBuilder;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ControllerEndPointAnalyzerTest {

    @Test
    public void analyze() {
        Trace trace = createValidTrace();
        ControllerEndPointAnalyzer analyzer = new ControllerEndPointAnalyzer();
        EndPointAnalysis analysis = analyzer.locateEndPoint(trace);
        assertNotNull(analysis);
        assertEquals("GET /path?fuu=bar", analysis.getExample());        
        assertEquals(EndPointName.valueOf("com.class.MyClass#method()"), analysis.getEndPointName());
        assertEquals("MyClass#method", analysis.getResourceLabel());
        assertEquals(1, analysis.getScore());
    }
    
    @Test
    public void analyze_topLevelNotHttp() {
        ControllerEndPointAnalyzer analyzer = new ControllerEndPointAnalyzer();
        
        SimpleFrameBuilder builder = new SimpleFrameBuilder();
        builder.enter(new Operation().type(ControllerEndPointAnalyzer.CONTROLLER_METHOD_TYPE));
        Frame root = builder.exit();
        Trace trace = mock(Trace.class);        
        when(trace.getRootFrame()).thenReturn(root);
        
        assertNull(analyzer.locateEndPoint(trace));
    }

    @Test
    public void analyze_noControllerFrame() {
        ControllerEndPointAnalyzer analyzer = new ControllerEndPointAnalyzer();
        
        Operation httpOp = new Operation().type(OperationType.HTTP);
        httpOp.createMap("request").put(OperationFields.URI, "/foo");

        Trace trace = mock(Trace.class);        
        SimpleFrameBuilder builder = new SimpleFrameBuilder();
        builder.enter(httpOp);
        Frame root = builder.exit();
        when(trace.getRootFrame()).thenReturn(root);
        when(trace.getAppName()).thenReturn(ApplicationName.valueOf("app"));
        
        assertNull(analyzer.locateEndPoint(trace));
    }
    
    public static Trace createValidTrace() {
        SimpleFrameBuilder builder = new SimpleFrameBuilder();
        Operation httpOp = new Operation().type(OperationType.HTTP);
        httpOp.createMap("request")
            .put(OperationFields.URI, "/path?fuu=bar")
            .put("method", "GET");
        builder.enter(httpOp);
        builder.enter(new Operation().type(ControllerEndPointAnalyzer.CONTROLLER_METHOD_TYPE)
                                     .put(OperationFields.CLASS_NAME, "com.class.MyClass")
                                     .put(OperationFields.METHOD_SIGNATURE, "method()")
                                     .label("MyClass#method"));
        builder.exit();
        Frame httpFrame = builder.exit();
        return Trace.newInstance(ApplicationName.valueOf("app"), TraceId.valueOf("0"), httpFrame);
    }
}
