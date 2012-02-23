/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.plugin.integration;

import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameUtil;
import com.springsource.insight.intercept.trace.Trace;

/**
 * {@link EndPointAnalyzer} for Spring Integration traces.
 * Integration 'Transformer' operations are never considered.
 *
 */
public class IntegrationEndPointAnalyzer implements EndPointAnalyzer {

    private static OperationType integrationType = OperationType.valueOf("integration_operation");
    
    public EndPointAnalysis locateEndPoint(Trace trace) {


        Frame si = trace.getFirstFrameOfType(integrationType);
        if (si == null) {
            return null;
        }
        
        Operation op = si.getOperation();

        String siComponentType = (String) op.get("siComponentType");

        if ("Transformer".equals(siComponentType)) {
            return null;
        }

        String opLabel = op.getLabel();
        EndPointName name = EndPointName.valueOf(opLabel);

        String label = name.getName();
        String exampleRequest = (String) op.get("siComponentType");
        int score = FrameUtil.getDepth(si);
        return new EndPointAnalysis(name, label, exampleRequest, score);
    }

}
