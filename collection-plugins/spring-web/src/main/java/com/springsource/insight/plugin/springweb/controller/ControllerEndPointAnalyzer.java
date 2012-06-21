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

package com.springsource.insight.plugin.springweb.controller;

import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameUtil;
import com.springsource.insight.intercept.trace.Trace;

/**
 * This trace analyzer simply looks at a Trace and returns a
 * ControllerEndPointAnalysis about what it found.
 * 
 * For a trace to be analyzed, it must be of the following format:
 * 
 * - HttpOperation 
 *    .. 
 *    .. (arbitrary nesting) 
 *      .. ControllerMethodOperation
 */
public class ControllerEndPointAnalyzer implements EndPointAnalyzer {
    private static final OperationType httpOpType = OperationType.HTTP;
    static final OperationType CONTROLLER_METHOD_TYPE = OperationType.valueOf("controller_method");

    /**
     * Returns a {@link EndPointAnalysis} object if the trace was
     * successfully determined to match the correct format (http -> controller)
     * 
     * Returns null otherwise.
     */
    public EndPointAnalysis locateEndPoint(Trace trace) {
        Frame controllerFrame = trace.getFirstFrameOfType(CONTROLLER_METHOD_TYPE);
        return makeEndPoint(controllerFrame, -1);
    }

    private EndPointAnalysis makeEndPoint(Frame controllerFrame, int currentScore) {
        if (controllerFrame == null) {
            return null;
        }
        
        Frame httpFrame = FrameUtil.getFirstParentOfType(controllerFrame, httpOpType);
        if (httpFrame == null) {
            return null;
        }

        Operation controllerOp = controllerFrame.getOperation();
        if (controllerOp == null) {
        	return null;
        }
        String examplePath = getExampleRequest(httpFrame);
        EndPointName endPointName = EndPointName.valueOf(controllerOp);
        String endPointLabel = controllerOp.getLabel();
        int	score = currentScore;
        if (score == -1) {
            score = FrameUtil.getDepth(controllerFrame);
        }

        return new EndPointAnalysis(endPointName, endPointLabel, examplePath, score, controllerOp);
    }

    public String getExampleRequest(Frame httpFrame) {
        Operation operation = httpFrame.getOperation();
        OperationMap details = operation.get("request", OperationMap.class);
        return details.get("method") + " " + details.get(OperationFields.URI);
    }

    public EndPointAnalysis locateEndPoint(Frame frame, int depth) {
        Frame parent = FrameUtil.getLastParentOfType(frame, CONTROLLER_METHOD_TYPE);
        
        if (parent != null) {
            return null;
        }
        
        return makeEndPoint(frame, depth);
    }

    public int getScore(Frame frame, int depth) {
        return depth;
    }

    public OperationType[] getOperationTypes() {
        return new OperationType[] {CONTROLLER_METHOD_TYPE};
    }
}
