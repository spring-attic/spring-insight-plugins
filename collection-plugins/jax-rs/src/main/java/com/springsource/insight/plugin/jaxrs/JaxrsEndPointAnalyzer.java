/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
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
import com.springsource.insight.intercept.trace.Frame;

/**
 */
public class JaxrsEndPointAnalyzer extends AbstractSingleTypeEndpointAnalyzer {
    private static final JaxrsEndPointAnalyzer INSTANCE = new JaxrsEndPointAnalyzer();

    private JaxrsEndPointAnalyzer() {
        super(JaxrsDefinitions.TYPE);
    }

    public static final JaxrsEndPointAnalyzer getInstance() {
        return INSTANCE;
    }

    @Override
    protected EndPointAnalysis makeEndPoint(Frame frame, int depth) {
        Operation op = frame.getOperation();
        EndPointName endPointName = EndPointName.valueOf(op);
        String example = EndPointAnalysis.getHttpExampleRequest(frame);
        return new EndPointAnalysis(endPointName, op.getLabel(), example, getOperationScore(op, depth), op);
    }
}
