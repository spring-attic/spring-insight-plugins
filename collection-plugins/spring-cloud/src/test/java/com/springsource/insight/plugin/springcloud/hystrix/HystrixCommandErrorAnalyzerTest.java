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

package com.springsource.insight.plugin.springcloud.hystrix;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameId;
import com.springsource.insight.intercept.trace.SimpleFrame;
import com.springsource.insight.intercept.trace.TraceError;
import com.springsource.insight.plugin.springcloud.SpringCloudPluginRuntimeDescriptor;
import com.springsource.insight.util.time.TimeRange;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;


public class HystrixCommandErrorAnalyzerTest {

    @Test
    public void testLocateFrameError_noEvents() throws Exception {

        Operation op = new Operation().type(SpringCloudPluginRuntimeDescriptor.HYSTRIX_COMMAND);
        Frame frame = new SimpleFrame(FrameId.valueOf(1), null, op , TimeRange.range(0, 10), Collections.EMPTY_LIST);
        HystrixCommandErrorAnalyzer analyzer = HystrixCommandErrorAnalyzer.getInstance();
        TraceError traceError = analyzer.locateFrameError(frame);
        assertNull(traceError);
    }

    @Test
    public void testLocateFrameError_notHystrixCommand() throws Exception {

        Operation op = new Operation().type(OperationType.valueOf("foobar"));
        Frame frame = new SimpleFrame(FrameId.valueOf(1), null, op , TimeRange.range(0, 10), Collections.EMPTY_LIST);
        HystrixCommandErrorAnalyzer analyzer = HystrixCommandErrorAnalyzer.getInstance();
        TraceError traceError = analyzer.locateFrameError(frame);
        assertNull(traceError);
    }

    @Test
    public void testLocateFrameError_validError() throws Exception {

        Operation op = new Operation().type(SpringCloudPluginRuntimeDescriptor.HYSTRIX_COMMAND);
        op.put("events", "TIMEOUT");
        Frame frame = new SimpleFrame(FrameId.valueOf(1), null, op , TimeRange.range(0, 10), Collections.EMPTY_LIST);
        HystrixCommandErrorAnalyzer analyzer = HystrixCommandErrorAnalyzer.getInstance();
        TraceError traceError = analyzer.locateFrameError(frame);
        assertNotNull(traceError);
        traceError.getMessage().contains("TIMEOUT");
    }

    @Test
    public void testLocateFrameError_validMultipleErrorFindsFirst() throws Exception {

        Operation op = new Operation().type(SpringCloudPluginRuntimeDescriptor.HYSTRIX_COMMAND);
        op.put("events", "TIMEOUT, SHORT_CIRCUITED");
        Frame frame = new SimpleFrame(FrameId.valueOf(1), null, op , TimeRange.range(0, 10), Collections.EMPTY_LIST);
        HystrixCommandErrorAnalyzer analyzer = HystrixCommandErrorAnalyzer.getInstance();
        TraceError traceError = analyzer.locateFrameError(frame);
        assertNotNull(traceError);
        traceError.getMessage().contains("TIMEOUT");
    }

    @Test
    public void testLocateFrameError_eventsNoErrors() throws Exception {

        Operation op = new Operation().type(SpringCloudPluginRuntimeDescriptor.HYSTRIX_COMMAND);
        op.put("events", "foobar, barfoo");
        Frame frame = new SimpleFrame(FrameId.valueOf(1), null, op , TimeRange.range(0, 10), Collections.EMPTY_LIST);
        HystrixCommandErrorAnalyzer analyzer = HystrixCommandErrorAnalyzer.getInstance();
        TraceError traceError = analyzer.locateFrameError(frame);
        assertNull(traceError);
    }

    @Test
    public void testLocateFrameError_noOperation() throws Exception {

        Frame frame = new SimpleFrame(FrameId.valueOf(1), null, null , TimeRange.range(0, 10), Collections.EMPTY_LIST);
        HystrixCommandErrorAnalyzer analyzer = HystrixCommandErrorAnalyzer.getInstance();
        TraceError traceError = analyzer.locateFrameError(frame);
        assertNull(traceError);
    }

    @Test
    public void testLocateFrameError_eventsNotString() throws Exception {
        Operation op = new Operation().type(SpringCloudPluginRuntimeDescriptor.HYSTRIX_COMMAND);
        op.putAny("events", new Object());
        Frame frame = new SimpleFrame(FrameId.valueOf(1), null, null , TimeRange.range(0, 10), Collections.EMPTY_LIST);
        HystrixCommandErrorAnalyzer analyzer = HystrixCommandErrorAnalyzer.getInstance();
        TraceError traceError = analyzer.locateFrameError(frame);
        assertNull(traceError);
    }
}