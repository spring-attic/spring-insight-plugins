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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.endpoint.AbstractEndPointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameUtil;
import com.springsource.insight.intercept.trace.Trace;

/**
 * 
 */
public class RunExecEndPointAnalyzer extends AbstractEndPointAnalyzer {
	/**
	 * The {@link List} of {@link OperationType}-s that mark a run/execute frame
	 */
	public static final List<OperationType>	OPS=Collections.unmodifiableList(Arrays.asList(RunExecDefinitions.EXEC_OP, RunExecDefinitions.RUN_OP));
    /**
     * The <U>static</U> score value assigned to endpoints - <B>Note:</B>
     * we return a score of {@link EndPointAnalysis#TOP_LAYER_SCORE} so as
     * to let other endpoints &quot;beat&quot; this one
     */
	public static final int	DEFAULT_SCORE=EndPointAnalysis.TOP_LAYER_SCORE;

    public RunExecEndPointAnalyzer() {
        super(OPS);
    }

    @Override
	public Frame getScoringFrame(Trace trace) {
		return resolveEndPointFrame(trace);
	}

	@Override
	protected int getDefaultScore(int depth) {
		return DEFAULT_SCORE;
    }

	@Override
	protected EndPointAnalysis makeEndPoint(Frame frame, int depth) {
        Operation   op=frame.getOperation();
        return new EndPointAnalysis(EndPointName.valueOf(op), op.getLabel(), op.getLabel(), getOperationScore(op, depth), op);
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
