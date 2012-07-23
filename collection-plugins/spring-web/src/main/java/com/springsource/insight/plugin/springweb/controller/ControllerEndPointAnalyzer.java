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

import com.springsource.insight.intercept.endpoint.AbstractSingleTypeEndpointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameUtil;

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
public class ControllerEndPointAnalyzer extends AbstractSingleTypeEndpointAnalyzer {
    public static final OperationType CONTROLLER_METHOD_TYPE = OperationType.valueOf("controller_method");

    public ControllerEndPointAnalyzer () {
    	super(CONTROLLER_METHOD_TYPE);
    }

    @Override
	protected EndPointAnalysis makeEndPoint(Frame controllerFrame, int score) {
        Operation controllerOp = controllerFrame.getOperation();
        Frame httpFrame = FrameUtil.getFirstParentOfType(controllerFrame, OperationType.HTTP);
        String examplePath = getExampleRequest(httpFrame, controllerOp);
        EndPointName endPointName = EndPointName.valueOf(controllerOp);
        String endPointLabel = controllerOp.getLabel();

        return new EndPointAnalysis(endPointName, endPointLabel, examplePath, score, controllerOp);
    }

    public String getExampleRequest(Frame httpFrame, Operation controllerOp) {
    	if (httpFrame != null) {
	        Operation operation = httpFrame.getOperation();
	        OperationMap details = operation.get("request", OperationMap.class);
	        return ((details == null) ? "???" : String.valueOf(details.get("method")))
                 + " " + ((details == null) ? "<UNKNOWN>" : details.get(OperationFields.URI));
    	}

    	return controllerOp.getLabel();
    }
}
