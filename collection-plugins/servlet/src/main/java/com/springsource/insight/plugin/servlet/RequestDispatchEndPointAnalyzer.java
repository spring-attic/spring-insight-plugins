/**
 * Copyright 2009-2010 the original author or authors.
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

import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;

public class RequestDispatchEndPointAnalyzer implements EndPointAnalyzer {
    public EndPointAnalysis locateEndPoint(Trace trace) {
        Frame dispatchFrame = trace.getFirstFrameOfType(OperationType.REQUEST_DISPATCH);
        if (dispatchFrame == null) {
            return null;
        }

        Operation op = dispatchFrame.getOperation();
        Boolean fromValue=op.get("from", Boolean.class);
        if ((fromValue == null) || (!fromValue.booleanValue())) {
            return null;
        }
        
        EndPointName name = EndPointName.valueOf(op.getLabel().replace('/', '_'));
        String endPointLabel = op.getLabel();
        String endPointExample = op.getLabel();
        
        return new EndPointAnalysis(name, endPointLabel, endPointExample, 0);
    }
}
