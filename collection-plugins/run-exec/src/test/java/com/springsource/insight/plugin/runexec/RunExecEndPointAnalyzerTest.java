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
package com.springsource.insight.plugin.runexec;

import java.util.Collections;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;

import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.server.ServerName;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameId;
import com.springsource.insight.intercept.trace.SimpleFrame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;
import com.springsource.insight.util.time.TimeRange;

/**
 * 
 */
public class RunExecEndPointAnalyzerTest extends Assert {
    private static final AtomicLong frameIdGenerator=new AtomicLong(0L);
    public RunExecEndPointAnalyzerTest() {
        super();
    }

    @Test
    public void testResolveEndPointFrameOnBothMissing () {
        assertNull(RunExecEndPointAnalyzer.resolveEndPointFrame(createTrace(createFrame(null, OperationType.valueOf("fake-op")))));
    }

    @Test
    public void testResolveEndPointFrameOnExecOnly () {
        assertResolutionResult(
                RunExecEndPointAnalyzer.resolveEndPointFrame(createTrace(createFrame(null, RunExecDefinitions.EXEC_OP))),
                RunExecDefinitions.EXEC_OP);
    }

    @Test
    public void testResolveEndPointFrameOnRunOnly () {
        assertResolutionResult(
                RunExecEndPointAnalyzer.resolveEndPointFrame(createTrace(createFrame(null, RunExecDefinitions.RUN_OP))),
                RunExecDefinitions.RUN_OP);
    }

    @Test
    public void testResolveEndPointFrameOnExecFirst () {
        Frame   execFrame=createFrame(null, RunExecDefinitions.EXEC_OP);
        createFrame(execFrame, RunExecDefinitions.RUN_OP);
        
        Frame   resFrame=RunExecEndPointAnalyzer.resolveEndPointFrame(createTrace(execFrame));
        assertSame("Mismatched resolved frame", execFrame, resFrame);
    }

    @Test
    public void testResolveEndPointFrameOnRunFirst () {
        Frame   runFrame=createFrame(null, RunExecDefinitions.RUN_OP);
        createFrame(runFrame, RunExecDefinitions.EXEC_OP);

        Frame   resFrame=RunExecEndPointAnalyzer.resolveEndPointFrame(createTrace(runFrame));
        assertSame("Mismatched resolved frame", runFrame, resFrame);
    }

    static void assertResolutionResult (Frame frame, OperationType expType) {
        assertNotNull("No frame", frame);
        
        Operation   op=frame.getOperation();
        assertNotNull("No operation", op);
        assertEquals("Mismatched operation type", expType, op.getType());
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
