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
 */
public class JaxrsEndPointAnalyzer implements EndPointAnalyzer {
    public JaxrsEndPointAnalyzer() {
        super();
    }

    public EndPointAnalysis locateEndPoint(Trace trace) {
        final Frame     frame=trace.getFirstFrameOfType(JaxrsDefinitions.TYPE);
        final Frame     rootFrame=trace.getRootFrame();
        
        return makeEndPoint(frame, rootFrame, -1);
    }

    private EndPointAnalysis makeEndPoint(Frame frame, Frame rootFrame, int score) {
        final Operation op=(frame == null) ? null : frame.getOperation();
        if (op == null) {
            return null;
        }

        final EndPointName  endPointName=EndPointName.valueOf(op);
        final Operation     rootOperation=rootFrame.getOperation();
        final Frame         httpFrame=FrameUtil.getFirstParentOfType(frame, OperationType.HTTP);
        final String        example=getExampleRequest(httpFrame, frame, rootOperation);
        
        if (score == -1) {
            score = FrameUtil.getDepth(frame);
        }
        
        return new EndPointAnalysis(endPointName, op.getLabel(), example, score, op);
    }

    public String getExampleRequest(Frame httpFrame, Frame frame, Operation rootOperation) {
        if (httpFrame == null) {
            return rootOperation.getLabel();
        }

        Operation operation = httpFrame.getOperation();
        OperationMap details = operation.get("request", OperationMap.class);
        return details.get("method") + " " + details.get(OperationFields.URI);
    }

    public EndPointAnalysis locateEndPoint(Frame frame, int depth) {
        Frame parent = FrameUtil.getLastParentOfType(frame, JaxrsDefinitions.TYPE);
        
        if (parent != null) {
            return null;
        }
        
        return makeEndPoint(frame, FrameUtil.getRoot(frame), depth);
    }

    public int getScore(Frame frame, int depth) {
        return depth;
    }

    public OperationType[] getOperationTypes() {
        return new OperationType[] {JaxrsDefinitions.TYPE};
    }
}
