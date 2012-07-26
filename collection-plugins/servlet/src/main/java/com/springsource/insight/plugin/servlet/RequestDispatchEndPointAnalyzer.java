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

import com.springsource.insight.intercept.endpoint.AbstractSingleTypeEndpointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;

public class RequestDispatchEndPointAnalyzer extends AbstractSingleTypeEndpointAnalyzer {
    /**
     * The <U>static</U> score value assigned to endpoints - <B>Note:</B>
     * we return a score of {@link EndPointAnalysis#TOP_LAYER_SCORE} so as
     * to let other endpoints &quot;beat&quot; this one
     */
	public static final int	DEFAULT_SCORE=EndPointAnalysis.TOP_LAYER_SCORE;

	public RequestDispatchEndPointAnalyzer () {
		super(OperationType.REQUEST_DISPATCH);
	}

	@Override
	public Frame getScoringFrame(Trace trace) {
		Frame	frame=super.getScoringFrame(trace);
		if (frame == null) {
			return frame;
		}

    	if (validateScoringFrame(frame) == null) {
    		return null;
    	} else {
    		return frame;
    	}
	}

    @Override
    protected int getDefaultScore(int depth) {
    	return DEFAULT_SCORE;
    }

	@Override
	protected OperationType validateScoringFrame(Frame frame) {
		OperationType	type=super.validateScoringFrame(frame);
		if (type == null) {
			return null;
		}

		Operation	op=frame.getOperation();
        Boolean 	fromValue=op.get("from", Boolean.class);
        if ((fromValue == null) || (!fromValue.booleanValue())) {
            return null;
        }

        return type;
	}

	@Override
	protected EndPointAnalysis makeEndPoint(Frame dispatchFrame, int depth) {
        Operation 		op=dispatchFrame.getOperation();
        String		 	label=op.getLabel();
        EndPointName	name=EndPointName.valueOf(label.replace('/', '_'));
        return new EndPointAnalysis(name, label, label, getOperationScore(op, depth), op);
    }
}
