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
package com.springsource.insight.plugin.servlet;

import org.junit.Assert;
import org.junit.Test;

import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.SimpleFrameBuilder;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;

public class LifecycleEndPointAnalyzerTest extends Assert {
    private static final ApplicationName app = ApplicationName.valueOf("app");
    private static final LifecycleEndPointAnalyzer analyzer = LifecycleEndPointAnalyzer.getInstance();

    public LifecycleEndPointAnalyzerTest () {
    	super();
    }

    @Test
    public void locateEndPoint() {
        Trace trace = createLifecycleEndPointTrace();
        EndPointAnalysis analysis = analyzer.locateEndPoint(trace);
        assertEquals("Mismatched label", LifecycleEndPointAnalyzer.ENDPOINT_LABEL, analysis.getResourceLabel());
        assertEquals("Mismatched endpoint name", LifecycleEndPointAnalyzer.ENDPOINT_NAME, analysis.getEndPointName());
        assertEquals("Mismatched example", "start", analysis.getExample());
        assertEquals("Mismatched score", LifecycleEndPointAnalyzer.ANALYSIS_SCORE, analysis.getScore());
    }
    
    @Test
    public void locateEndPoint_noHttp() {
        Trace trace = createNonLifecycleTrace();
        EndPointAnalysis	result=analyzer.locateEndPoint(trace);
        assertNull("Unexpected analysis result: " + result, result);
    }
    
    private Trace createNonLifecycleTrace() {
        SimpleFrameBuilder builder = new SimpleFrameBuilder();
        builder.enter(new Operation());
        Frame topLevelFrame = builder.exit();
        return Trace.newInstance(app, TraceId.valueOf("0"), topLevelFrame);
    }
    
    private Trace createLifecycleEndPointTrace() {
        Operation operation = new Operation()
            .type(LifecycleEndPointAnalyzer.SERVLET_LISTENER_TYPE)
            .put("event", "start");
        SimpleFrameBuilder builder = new SimpleFrameBuilder();
        builder.enter(operation);
        Frame httpFrame = builder.exit();
        return Trace.newInstance(app, TraceId.valueOf("0"), httpFrame);
    }

}
