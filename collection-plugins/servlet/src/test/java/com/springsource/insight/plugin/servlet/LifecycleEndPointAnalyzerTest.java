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

package com.springsource.insight.plugin.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.SimpleFrameBuilder;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;

public class LifecycleEndPointAnalyzerTest {
    
    private ApplicationName app = ApplicationName.valueOf("app");
    
    @Test
    public void locateEndPoint() {
        LifecycleEndPointAnalyzer analyzer = new LifecycleEndPointAnalyzer();
        Trace trace = createLifecycleEndPointTrace();
        EndPointAnalysis analysis = analyzer.locateEndPoint(trace);
        assertEquals("Lifecycle", analysis.getResourceLabel());
        assertEquals("lifecycle", analysis.getEndPointName().getName());
        assertEquals("start", analysis.getExample());
    }
    
    @Test
    public void locateEndPoint_noHttp() {
        LifecycleEndPointAnalyzer analyzer = new LifecycleEndPointAnalyzer();
        Trace trace = createNonLifecycleTrace();
        assertNull(analyzer.locateEndPoint(trace));
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
