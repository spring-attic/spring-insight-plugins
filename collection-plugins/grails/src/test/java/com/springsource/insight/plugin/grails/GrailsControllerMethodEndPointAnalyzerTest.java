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

package com.springsource.insight.plugin.grails;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.operation.SourceCodeLocation;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameBuilder;
import com.springsource.insight.intercept.trace.SimpleFrameBuilder;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;

public class GrailsControllerMethodEndPointAnalyzerTest {
    
    private static final OperationType TYPE = OperationType.valueOf("grails_controller_method");
    
    private ApplicationName app = ApplicationName.valueOf("app");    
    private GrailsControllerMethodEndPointAnalyzer endPointAnalyzer;
    private Operation grailsOp;
    
    @Before
    public void setUp() {
        endPointAnalyzer = new GrailsControllerMethodEndPointAnalyzer();
        grailsOp = new Operation().type(TYPE);
    }
    
    @Test
    public void locateEndPoint_noHttp() {
        FrameBuilder b = new SimpleFrameBuilder();
        b.enter(new Operation());
        b.enter(grailsOp);
        Frame grailsFrame = b.exit();
        Frame rootFrame = b.exit();
        Trace trace = Trace.newInstance(app, TraceId.valueOf("0"), rootFrame);
        assertNull(endPointAnalyzer.locateEndPoint(trace));
    }
    
    @Test
    public void locateEndPoint_noGrails() {
        FrameBuilder b = new SimpleFrameBuilder();
        Operation httpOp = new Operation().type(OperationType.HTTP);
        b.enter(httpOp);
        b.enter(new Operation());
        Frame simpleFrame = b.exit();
        Frame httpFrame = b.exit();
        Trace trace = Trace.newInstance(app, TraceId.valueOf("0"), httpFrame);
        assertEquals(null, endPointAnalyzer.locateEndPoint(trace));
    }
    
    @Test
    public void locateEndPoint_httpMustComeBeforeGrails() {
        FrameBuilder b = new SimpleFrameBuilder();
        b.enter(grailsOp);
        Operation httpOp = new Operation().type(OperationType.HTTP);
        b.enter(httpOp);
        Frame httpFrame = b.exit();
        Frame grailsFrame = b.exit();
        Trace trace = Trace.newInstance(app, TraceId.valueOf("0"), grailsFrame);
        assertEquals(null, endPointAnalyzer.locateEndPoint(trace));
    }

    @Test
    public void locateEndPoint() {
        grailsOp.label("MyController#myAction")
            .sourceCodeLocation(new SourceCodeLocation("org.shortController", "myAction", 111));
        FrameBuilder b = new SimpleFrameBuilder();
        Operation httpOp = new Operation().type(OperationType.HTTP);
        b.enter(httpOp);
        b.enter(grailsOp);
        Frame grailsFrame = b.exit();        
        Frame httpFrame = b.exit();
        Trace trace = Trace.newInstance(app, TraceId.valueOf("0"), httpFrame);
        EndPointAnalysis endPoint = endPointAnalyzer.locateEndPoint(trace); 
        assertEquals("org.shortController.myAction", endPoint.getEndPointName().getName());
        assertEquals("MyController#myAction", endPoint.getResourceLabel());
    }
    
}
