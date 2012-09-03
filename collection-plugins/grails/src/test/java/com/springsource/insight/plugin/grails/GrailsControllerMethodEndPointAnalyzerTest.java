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

package com.springsource.insight.plugin.grails;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.operation.SourceCodeLocation;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameBuilder;
import com.springsource.insight.intercept.trace.FrameUtil;
import com.springsource.insight.intercept.trace.SimpleFrameBuilder;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;

public class GrailsControllerMethodEndPointAnalyzerTest extends Assert {
    private static final ApplicationName app = ApplicationName.valueOf("app");    
    private static final GrailsControllerMethodEndPointAnalyzer endPointAnalyzer=new GrailsControllerMethodEndPointAnalyzer();
    private Operation grailsOp;
    
    public GrailsControllerMethodEndPointAnalyzerTest () {
    	super();
    }

    @Before
    public void setUp() {
        grailsOp = new Operation()
        		.type(GrailsControllerMethodEndPointAnalyzer.TYPE)
        		.label("MyController#myAction")
                .sourceCodeLocation(new SourceCodeLocation("org.shortController", "myAction", 111))
                ;
    }

    @Test
    public void locateEndPointNoGrails() {
        FrameBuilder b = new SimpleFrameBuilder();
        Operation httpOp = new Operation().type(OperationType.HTTP);
        b.enter(httpOp);
        b.enter(new Operation());
        Frame simpleFrame = b.exit();
        assertNotNull("No simple frame", simpleFrame);
        Frame httpFrame = b.exit();
        Trace trace = Trace.newInstance(app, TraceId.valueOf("0"), httpFrame);
        EndPointAnalysis	ep=endPointAnalyzer.locateEndPoint(trace);
        assertNull("Unexpected result: " + ep, ep);
    }
    
    @Test
    public void locateEndPointNoHttp() {
        FrameBuilder b = new SimpleFrameBuilder();
        b.enter(new Operation());
        b.enter(grailsOp);
        Frame grailsFrame = b.exit();
        assertNotNull("No grails frame", grailsFrame);
        Frame rootFrame = b.exit();
        Trace trace = Trace.newInstance(app, TraceId.valueOf("0"), rootFrame);
        assertEndpointAnalysis(trace, grailsFrame, null);
    }
    
    @Test
    public void locateEndPointHttpComesBeforeGrails() {
        FrameBuilder b = new SimpleFrameBuilder();
        b.enter(grailsOp);
        Operation httpOp = new Operation().type(OperationType.HTTP);
        b.enter(httpOp);
        Frame httpFrame = b.exit();
        assertNotNull("No http frame", httpFrame);
        Frame grailsFrame = b.exit();
        Trace trace = Trace.newInstance(app, TraceId.valueOf("0"), grailsFrame);
        assertEndpointAnalysis(trace, grailsFrame, null);
    }

    @Test
    public void locateEndPointHttpComesAfterGrails() {
        FrameBuilder b = new SimpleFrameBuilder();
        Operation httpOp = new Operation().type(OperationType.HTTP);
        b.enter(httpOp);
        b.enter(grailsOp);
        Frame grailsFrame = b.exit();        
        assertNotNull("No grails frame", grailsFrame);
        Frame httpFrame = b.exit();
        Trace trace = Trace.newInstance(app, TraceId.valueOf("0"), httpFrame);
        assertEndpointAnalysis(trace, grailsFrame, httpFrame); 
    }

    private static EndPointAnalysis assertEndpointAnalysis (Trace trace, Frame grailsFrame, Frame httpFrame) {
    	EndPointAnalysis	ep=endPointAnalyzer.locateEndPoint(trace);
    	assertNotNull("No analysis", ep);

    	Operation 		operation = grailsFrame.getOperation();
        String 			resourceKey = GrailsControllerMethodEndPointAnalyzer.makeResourceKey(operation.getSourceCodeLocation());
        EndPointName	epName=EndPointName.valueOf(resourceKey);
        assertEquals("Mismatched endpoint name", epName, ep.getEndPointName());

    	assertEquals("Mismatched label", operation.getLabel(), ep.getResourceLabel());
    	assertEquals("Mismatched score", EndPointAnalysis.depth2score(FrameUtil.getDepth(grailsFrame)), ep.getScore());

    	if (httpFrame == null) {
    		assertEquals("Mismatched grails example", operation.getLabel(), ep.getExample());
    	} else {
    		String	expected=EndPointAnalysis.createHttpExampleRequest(httpFrame);
    		assertEquals("Mismatched http example", expected, ep.getExample());
    	}

    	return ep;
    }

}
