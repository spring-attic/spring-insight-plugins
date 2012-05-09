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
        Frame httpFrame = trace.getFirstFrameOfType(httpOpType);
        if (httpFrame == null) {
            return null;
        }

        Frame controllerFrame = trace.getFirstFrameOfType(CONTROLLER_METHOD_TYPE);
        if (controllerFrame == null || !FrameUtil.frameIsAncestor(httpFrame, controllerFrame)) {
            return null;
        }

        Operation controllerOp = controllerFrame.getOperation();
        if (controllerOp != null) {
            String examplePath = getExampleRequest(httpFrame);
            EndPointName endPointName = EndPointName.valueOf(controllerOp);
            String endPointLabel = controllerOp.getLabel();            
            int score = FrameUtil.getDepth(controllerFrame);
            return new EndPointAnalysis(endPointName, endPointLabel, examplePath, score, controllerOp);
        }
        return null;
    }

    public String getExampleRequest(Frame httpFrame) {
        Operation operation = httpFrame.getOperation();
        OperationMap details = operation.get("request", OperationMap.class);
        return details.get("method") + " " + details.get(OperationFields.URI);
    }
}
