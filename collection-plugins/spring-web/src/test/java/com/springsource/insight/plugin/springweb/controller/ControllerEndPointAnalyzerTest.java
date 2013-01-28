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

package com.springsource.insight.plugin.springweb.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameId;
import com.springsource.insight.intercept.trace.FrameUtil;
import com.springsource.insight.intercept.trace.SimpleFrame;
import com.springsource.insight.intercept.trace.SimpleFrameBuilder;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;
import com.springsource.insight.util.time.TimeRange;

public class ControllerEndPointAnalyzerTest extends Assert {
    private static final ControllerEndPointAnalyzer analyzer = ControllerEndPointAnalyzer.getInstance();
    private static final String	TEST_VERB="GET", TEST_PATH="/path?fuu=bar";
    private static final String	TEST_CLASS_NAME="MyClass", TEST_METHOD_NAME="method";
    private static final String	TEST_CLASS_PATH="com.class." + TEST_CLASS_NAME, TEST_SIGNATURE=TEST_METHOD_NAME + "()";

    public ControllerEndPointAnalyzerTest () {
        super();
    }

    @Test
    public void testDefaultAnalysis() {
        Trace 	trace=createValidTrace(false);
        Frame	scoreFrame=analyzer.getScoringFrame(trace);
        assertEndPointAnalysis("traceAnalysis", analyzer.locateEndPoint(trace), scoreFrame);
        assertEndPointAnalysis("frameAnalysis", analyzer.locateEndPoint(scoreFrame, FrameUtil.getDepth(scoreFrame)), scoreFrame);
    }

    @Test
    public void testLegacyAnalysis() {
        Trace 	trace=createValidTrace(false);
        Frame	scoreFrame=analyzer.getScoringFrame(trace);
        assertEndPointAnalysis("traceAnalysis", analyzer.locateEndPoint(trace), scoreFrame);
        assertEndPointAnalysis("frameAnalysis", analyzer.locateEndPoint(scoreFrame, FrameUtil.getDepth(scoreFrame)), scoreFrame);
    }

    @Test
    public void testAnalyzeNoHttpFrame() {
        Operation	op=createControllerOperation(false);
        Frame		frame=new SimpleFrame(FrameId.valueOf("3777347"), null, op, new TimeRange(1L, 10L), Collections.<Frame>emptyList());
        Trace		trace=Trace.newInstance(ApplicationName.valueOf("app"), TraceId.valueOf("0"), frame);
        EndPointAnalysis	analysis=analyzer.locateEndPoint(trace);
        assertNotNull("No analysis result", analysis);
        assertEquals("Mismatched example", op.getLabel(), analysis.getExample());
    }

    @Test
    public void testAnalyzeNoControllerFrame() {
        Operation httpOp = new Operation().type(OperationType.HTTP);
        httpOp.createMap("request").put(OperationFields.URI, "/foo");

        Trace trace = mock(Trace.class);
        SimpleFrameBuilder builder = new SimpleFrameBuilder();
        builder.enter(httpOp);
        Frame root = builder.exit();
        when(trace.getRootFrame()).thenReturn(root);
        when(trace.getAppName()).thenReturn(ApplicationName.valueOf("app"));

        EndPointAnalysis analysis=analyzer.locateEndPoint(trace);
        assertNull("Unexpected result: " + analysis, analysis);
    }

    private static EndPointAnalysis assertEndPointAnalysis (String testName, EndPointAnalysis analysis, Frame scoreFrame) {
        assertNotNull(testName + ": No analysis result", analysis);
        assertEquals(testName + ": Mismatched example", TEST_VERB + " " + TEST_PATH, analysis.getExample());
        assertEquals(testName + ": Mismatched endpoint", EndPointName.valueOf(TEST_CLASS_PATH + "#" + TEST_SIGNATURE), analysis.getEndPointName());
        assertEquals(testName + ": Mismatched label", TEST_CLASS_NAME + "#" + TEST_METHOD_NAME, analysis.getResourceLabel());

        Operation	op=scoreFrame.getOperation();
        Boolean		legacy=op.get(ControllerEndPointAnalyzer.LEGACY_PROPNAME, Boolean.class);
        if ((legacy != null) && legacy.booleanValue()) {
            assertEquals(testName + ": Mismatched legacy score", ControllerEndPointAnalyzer.LEGACY_SCORE, analysis.getScore());
        } else {
            assertEquals(testName + ": Mismatched default score", ControllerEndPointAnalyzer.DEFAULT_CONTROLLER_SCORE, analysis.getScore());
        }
        return analysis;
    }

    public static Trace createValidTrace(boolean legacy) {
        SimpleFrameBuilder builder = new SimpleFrameBuilder();
        Operation httpOp = new Operation().type(OperationType.HTTP);
        httpOp.createMap("request")
        .put(OperationFields.URI, TEST_PATH)
        .put("method", TEST_VERB);
        builder.enter(httpOp);
        builder.enter(createControllerOperation(legacy));
        builder.exit();
        Frame httpFrame = builder.exit();
        return Trace.newInstance(ApplicationName.valueOf("app"), TraceId.valueOf("0"), httpFrame);
    }

    private static Operation createControllerOperation (boolean legacy) {
        return new Operation()
        .type(ControllerEndPointAnalyzer.CONTROLLER_METHOD_TYPE)
        .label(TEST_CLASS_NAME + "#" + TEST_METHOD_NAME)
        .put(OperationFields.CLASS_NAME, TEST_CLASS_PATH)
        .put(OperationFields.METHOD_SIGNATURE, TEST_SIGNATURE)
        .put(ControllerEndPointAnalyzer.LEGACY_PROPNAME, legacy)
        ;
    }
}
