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
package com.springsource.insight.plugin.jaxrs;

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
 */
public class JaxrsEndPointAnalyzer extends AbstractSingleTypeEndpointAnalyzer {
    public JaxrsEndPointAnalyzer() {
        super(JaxrsDefinitions.TYPE);
    }

    @Override
	protected EndPointAnalysis makeEndPoint(Frame frame, int depth) {
		Operation	  op=frame.getOperation();
        EndPointName  endPointName=EndPointName.valueOf(op);
        Frame 		  rootFrame=FrameUtil.getRoot(frame);
        Operation     rootOperation=rootFrame.getOperation();
        Frame         httpFrame=FrameUtil.getFirstParentOfType(frame, OperationType.HTTP);
        String        example=getExampleRequest(httpFrame, frame, rootOperation);
        return new EndPointAnalysis(endPointName, op.getLabel(), example, EndPointAnalysis.depth2score(depth), op);
    }

    public String getExampleRequest(Frame httpFrame, Frame frame, Operation rootOperation) {
        if (httpFrame == null) {
            return rootOperation.getLabel();
        }

        Operation operation = httpFrame.getOperation();
        OperationMap details = operation.get("request", OperationMap.class);
        return ((details == null) ? "???" : String.valueOf(details.get("method")))
             + " " + ((details == null) ? "<UNKNOWN>" : details.get(OperationFields.URI));
    }
}
