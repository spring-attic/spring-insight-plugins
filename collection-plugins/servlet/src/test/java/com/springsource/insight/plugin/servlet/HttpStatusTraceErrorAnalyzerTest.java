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
package com.springsource.insight.plugin.servlet;

import java.util.List;

import org.junit.Test;

import com.springsource.insight.collection.test.AbstractCollectionTestSupport;
import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.SimpleFrameBuilder;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceError;
import com.springsource.insight.intercept.trace.TraceId;

public class HttpStatusTraceErrorAnalyzerTest extends AbstractCollectionTestSupport {
    @Test
    public void findErrors_noHttpFrame() {
        SimpleFrameBuilder builder = new SimpleFrameBuilder();
        Operation op = new Operation();
        builder.enter(op);
        Frame frame = builder.exit();
        Trace trace = Trace.newInstance(ApplicationName.valueOf("app"), TraceId.valueOf("0"), frame);
        HttpStatusTraceErrorAnalyzer analyzer = new HttpStatusTraceErrorAnalyzer();
        assertTrue(analyzer.locateErrors(trace).isEmpty());
    }
    
    @Test
    public void findErrors_noErrors() {
        Trace trace = createHttpTrace(200, true);
        HttpStatusTraceErrorAnalyzer analyzer = new HttpStatusTraceErrorAnalyzer();
        assertTrue(analyzer.locateErrors(trace).isEmpty());
    }
    
    @Test
    public void findErrors() {
        Trace trace = createHttpTrace(503, true);
        HttpStatusTraceErrorAnalyzer analyzer = new HttpStatusTraceErrorAnalyzer();
        List<TraceError> errors = analyzer.locateErrors(trace);
        assertEquals(1, errors.size());
        TraceError error = errors.get(0);
        assertTrue(error.getMessage().contains("503"));
    }

    @Test
    public void findContextNotAvailableErrors() {
        Trace trace = createHttpTrace(200, false);
        HttpStatusTraceErrorAnalyzer analyzer = new HttpStatusTraceErrorAnalyzer();
        List<TraceError> errors = analyzer.locateErrors(trace);
        assertEquals(1, errors.size());
        TraceError error = errors.get(0);
        assertTrue(error.getMessage().contains("Context not available"));
    }

    Trace createHttpTrace(int statusCode, boolean available) {
        SimpleFrameBuilder builder = new SimpleFrameBuilder();
        Operation httpOp = new Operation()
            .type(OperationType.HTTP);
        httpOp.createMap("request")
            .put(OperationFields.URI, "/path?fuu=bar")
            .put("method", "GET")
            .put(OperationFields.CONTEXT_AVAILABLE, available)
            .put("servletName", "My stuff / servlet");
        httpOp.createMap("response")
            .put("statusCode", statusCode);
        builder.enter(httpOp);
        Frame httpFrame = builder.exit();
        return Trace.newInstance(ApplicationName.valueOf("app"), TraceId.valueOf("0"), httpFrame);
    }

    @Test
    public void httpStatusIsError() throws Exception {
        HttpStatusTraceErrorAnalyzer analyzer = new HttpStatusTraceErrorAnalyzer();
        
        for (int status=0; status<800; status++) {
            boolean error = analyzer.httpStatusIsError(status);
            
            if (status >= 500 && status < 600) {
                assertTrue(error);
            } else {
                assertFalse(error);
            }
        }
    }
    
}
