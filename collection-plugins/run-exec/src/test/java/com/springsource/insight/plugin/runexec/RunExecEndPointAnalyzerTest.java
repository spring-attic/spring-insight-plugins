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
package com.springsource.insight.plugin.runexec;

import java.util.Collections;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.springsource.insight.collection.test.AbstractCollectionTestSupport;
import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.server.ServerName;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameId;
import com.springsource.insight.intercept.trace.FrameUtil;
import com.springsource.insight.intercept.trace.SimpleFrame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;
import com.springsource.insight.util.time.TimeRange;

/**
 * 
 */
public class RunExecEndPointAnalyzerTest extends AbstractCollectionTestSupport {
    private static final AtomicLong frameIdGenerator=new AtomicLong(0L);
    private static final RunExecEndPointAnalyzer	analyzer=RunExecEndPointAnalyzer.getInstance();

    public RunExecEndPointAnalyzerTest() {
        super();
    }

    @Test
    public void testResolveEndPointFrameOnBothMissing () {
    	Trace	trace=createTrace(createFrame(null, OperationType.valueOf("fake-op")));
    	Frame	frame=RunExecEndPointAnalyzer.resolveEndPointFrame(trace);
        assertNull("Unexpected resolved frame: " + frame, frame);

        frame = analyzer.getScoringFrame(trace);
        assertNull("Unexpected scoring frame: " + frame, frame);

        EndPointAnalysis	endPoint=analyzer.locateEndPoint(trace);
        assertNull("Unexpected endpoint: " + endPoint, endPoint);
    }

    @Test
    public void testResolveEndPointFrameOnExecOnly () {
    	Trace	trace=createTrace(createFrame(null, RunExecDefinitions.EXEC_OP));
        Frame	frame=assertResolutionResult(RunExecEndPointAnalyzer.resolveEndPointFrame(trace), RunExecDefinitions.EXEC_OP);
        assertAnalysisResult(trace, frame);
    }

    @Test
    public void testResolveEndPointFrameOnRunOnly () {
    	Trace	trace=createTrace(createFrame(null, RunExecDefinitions.RUN_OP));
        Frame	frame=assertResolutionResult(RunExecEndPointAnalyzer.resolveEndPointFrame(trace), RunExecDefinitions.RUN_OP);
        assertAnalysisResult(trace, frame);
    }

    @Test
    public void testResolveEndPointFrameOnExecFirst () {
        Frame   execFrame=createFrame(null, RunExecDefinitions.EXEC_OP);
        Frame	runFrame=createFrame(execFrame, RunExecDefinitions.RUN_OP);
        assertNotNull("No run frame created", runFrame);

        Trace	trace=createTrace(execFrame);
        Frame   resFrame=RunExecEndPointAnalyzer.resolveEndPointFrame(trace);
        assertSame("Mismatched resolved frame", execFrame, resFrame);
        assertAnalysisResult(trace, resFrame);
    }

    @Test
    public void testResolveEndPointFrameOnRunFirst () {
        Frame   runFrame=createFrame(null, RunExecDefinitions.RUN_OP);
        Frame	execFrame=createFrame(runFrame, RunExecDefinitions.EXEC_OP);
        assertNotNull("No exec frame created", execFrame);

        Trace	trace=createTrace(runFrame);
        Frame   resFrame=RunExecEndPointAnalyzer.resolveEndPointFrame(trace);
        assertSame("Mismatched resolved frame", runFrame, resFrame);
        assertAnalysisResult(trace, resFrame);
    }

    static Frame assertResolutionResult (Frame frame, OperationType expType) {
        assertNotNull("No frame", frame);
        
        Operation   op=frame.getOperation();
        assertNotNull("No operation", op);
        assertEquals("Mismatched operation type", expType, op.getType());
        return frame;
    }

    static EndPointAnalysis assertAnalysisResult (Trace trace, Frame scoreFrame) {
    	EndPointAnalysis	a1=analyzer.locateEndPoint(trace);
    	assertNotNull("No analysis extracted for trace=" + trace, a1);
    	assertAnalysisResult(a1, scoreFrame);

    	EndPointAnalysis	a2=analyzer.locateEndPoint(scoreFrame, FrameUtil.getDepth(scoreFrame));
    	assertEquals("Mismatched frame analysis result", a1, a2);
    	return a1;
    }

    static Operation assertAnalysisResult (EndPointAnalysis a, Frame frame) {
        Operation   op=frame.getOperation();
        assertEquals("Mismatched endpoint name", EndPointName.valueOf(op), a.getEndPointName());
        assertEquals("Mismatched label", op.getLabel(), a.getResourceLabel());
        assertEquals("Mismatched example", op.getLabel(), a.getExample());
        assertEquals("Mismatched score", RunExecEndPointAnalyzer.DEFAULT_SCORE, a.getScore());
        return op;

    }
    static Frame createFrame (Frame parent, OperationType opType) {
        Operation op = new Operation().type(opType);
        
        return new SimpleFrame(FrameId.valueOf(String.valueOf(frameIdGenerator.incrementAndGet())),
                               parent,
                               op,
                               TimeRange.milliTimeRange(0, 1),
                               Collections.<Frame>emptyList());
    }
    
    static Trace createTrace (Frame root) {
        return new Trace(ServerName.valueOf("fake-server"),
                         ApplicationName.valueOf("fake-app"),
                         new Date(System.currentTimeMillis()),
                         TraceId.valueOf("fake-id"),
                         root);
    }

}
