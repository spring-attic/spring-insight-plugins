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

/**
 * Locates servlet endpoints within Traces.  
 * 
 * If an HttpOperation is detected, an endpoint analysis will be returned.
 * The score of the analysis will always be 0, and its endpoint key/label
 * will be based on the servlet's name.
 */
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;

public class ServletEndPointAnalyzer implements EndPointAnalyzer {
    private static final int ANALYSIS_SCORE = 0;

    public EndPointAnalysis locateEndPoint(Trace trace) {
        Frame httpFrame = trace.getFirstFrameOfType(OperationType.HTTP);
        if (httpFrame == null) {
            return null;
        }

        return makeEndPoint(httpFrame);
    }

    private EndPointAnalysis makeEndPoint(Frame httpFrame) {
        Operation op = httpFrame.getOperation();
        OperationMap request = op.get("request", OperationMap.class);
        
        String servletName = request.get("servletName", String.class);
        String endPointKey = sanitizeEndPointKey(servletName);
        String endPointLabel = "Servlet: " + servletName;

        return new EndPointAnalysis(EndPointName.valueOf(endPointKey), endPointLabel,
                                    getExampleRequest(op), ANALYSIS_SCORE);
    }

    String sanitizeEndPointKey(String endPointKey) {
        return endPointKey.replace('/', '_');
    }

    String getExampleRequest(Operation op) {
        OperationMap request = op.get("request", OperationMap.class);
        return request.get("method") + " " + request.get(OperationFields.URI);
    }
}
