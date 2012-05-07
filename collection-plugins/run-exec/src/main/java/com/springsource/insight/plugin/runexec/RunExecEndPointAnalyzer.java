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

import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameUtil;
import com.springsource.insight.intercept.trace.Trace;

/**
 * 
 */
public class RunExecEndPointAnalyzer implements EndPointAnalyzer {
    public RunExecEndPointAnalyzer() {
        super();
    }

    public EndPointAnalysis locateEndPoint(Trace trace) {
        Frame       frame=resolveEndPointFrame(trace);
        Operation   op=(frame == null) ? null : frame.getOperation();
        if (op == null) {
            return null;
        } else {
            // NOTE: we return a score of zero so as to let other endpoints "beat" this one
            return new EndPointAnalysis(EndPointName.valueOf(op), op.getLabel(), op.getLabel(), 0);
        }
    }

    static Frame resolveEndPointFrame (Trace trace) {
        Frame       execFrame=trace.getFirstFrameOfType(RunExecDefinitions.EXEC_OP),
                    runFrame=trace.getFirstFrameOfType(RunExecDefinitions.RUN_OP);
        Operation   opExec=(execFrame == null) ? null : execFrame.getOperation(),
                    opRun=(runFrame == null) ? null : runFrame.getOperation();
        if (opExec == null) {
            if (opRun == null) {
                return null;
            } else {
                return runFrame;
            }
        } else if (opRun == null) {
            return execFrame;
        }

        // at this stage we have 2 frames - prefer the one higher up the trace
        int execDepth = FrameUtil.getDepth(execFrame), runDepth = FrameUtil.getDepth(runFrame);
        if (execDepth < runDepth) {
            return execFrame;
        } else {
            return runFrame;
        }
    }
}
