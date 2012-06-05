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

/**
 * Locates lifecycle endpoints within Traces.  
 * 
 * If a LifecycleOperation is detected in the root frame, an endpoint analysis 
 * will be returned. The score of the analysis will always be {@link LifecycleEndPointAnalyzer#ANALYSIS_SCORE}, and its endpoint
 * key/label will be based on the lifecycle event.
 */
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;

public class LifecycleEndPointAnalyzer implements EndPointAnalyzer {
    private static final int ANALYSIS_SCORE = 50;
    public static final OperationType SERVLET_LISTENER_TYPE = OperationType.valueOf("servlet-listener");
    public static final OperationType LIFECYCLE_TYPE_TYPE = OperationType.APP_LIFECYCLE;

    public EndPointAnalysis locateEndPoint(Trace trace) {
        Frame rootFrame = trace.getRootFrame();
        OperationType rootType = rootFrame.getOperation().getType();
        if (rootType.equals(SERVLET_LISTENER_TYPE) ||
            rootType.equals(LIFECYCLE_TYPE_TYPE))
        {
            return makeEndPoint(rootFrame);
        }

        return null;
    }

    private EndPointAnalysis makeEndPoint(Frame lifecycleFrame) {
        Operation op = lifecycleFrame.getOperation();
        
        String endPointName = "lifecycle";
        String endPointLabel = "Lifecycle";
        String endPointExample = op.get("event", String.class);

        return new EndPointAnalysis(EndPointName.valueOf(endPointName), endPointLabel,
                                    endPointExample, ANALYSIS_SCORE, op);
    }

    public EndPointAnalysis locateEndPoint(Frame frame, int depth) {
        if (frame.isRoot()) {
            makeEndPoint(frame);
        }
        return null;
    }

    public int getScore(Frame frame, int depth) {
        return ANALYSIS_SCORE;
    }

    public OperationType[] getOperationTypes() {
        return new OperationType[] {SERVLET_LISTENER_TYPE, LIFECYCLE_TYPE_TYPE};
    }
}
