/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.plugin.jms;


import com.springsource.insight.intercept.endpoint.AbstractSingleTypeEndpointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.SourceCodeLocation;
import com.springsource.insight.intercept.operation.method.JoinPointBreakDown;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.util.StringUtil;

public class JMSEndPointAnalyzer extends AbstractSingleTypeEndpointAnalyzer {

    private static final JMSEndPointAnalyzer INSTANCE = new JMSEndPointAnalyzer();

    public static final JMSEndPointAnalyzer getInstance() {
        return INSTANCE;
    }
    protected JMSEndPointAnalyzer() {
        super(JMSPluginOperationType.RECEIVE.getOperationType());
    }

    @Override
    protected EndPointAnalysis makeEndPoint(Frame frame, int depth) {
        Operation operation = frame.getOperation();
        EndPointName endPointName = EndPointName.valueOf(EndPointName.createEndpointName(operation));
        String example = createExample(operation);
        return new EndPointAnalysis(endPointName, getLabel(operation), example, EndPointAnalysis.CEILING_LAYER_SCORE + 1);
    }

    private String createExample(Operation op) {
        String type = op.get("destinationType", String.class);
        String name = op.get("destinationName", String.class);
        if (!StringUtil.isEmpty(name))
            return "Received message from " + type + "-" + name;
        else
            return "Received message from " + type;
    }

    private String getLabel(Operation op) {
        SourceCodeLocation sourceCodeLocation = op.getSourceCodeLocation();
        return JoinPointBreakDown.getShortClassName(sourceCodeLocation.getClassName()) + "#" + sourceCodeLocation.getMethodName();
    }
}
