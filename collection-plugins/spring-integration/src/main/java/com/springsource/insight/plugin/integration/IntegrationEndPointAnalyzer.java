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
package com.springsource.insight.plugin.integration;

import com.springsource.insight.intercept.endpoint.AbstractSingleTypeEndpointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;

/**
 * {@link EndPointAnalyzer} for Spring Integration traces.
 * Integration 'Transformer' operations are never considered.
 *
 */
public class IntegrationEndPointAnalyzer extends AbstractSingleTypeEndpointAnalyzer {
    public static final OperationType integrationType = OperationType.valueOf("integration_operation");
    
    public IntegrationEndPointAnalyzer () {
    	super(integrationType);
    }

    @Override
	public Frame getScoringFrame(Trace trace) {
    	Frame	frame=super.getScoringFrame(trace);
    	if (frame == null) {
    		return null;
    	}

    	if (validateScoringFrame(frame) == null) {
    		return null;
    	}

    	return frame;
    }

    @Override
	protected OperationType validateScoringFrame(Frame frame) {
		OperationType	type=super.validateScoringFrame(frame);
		if (type == null) {
			return null;
		}

        Operation 	op=frame.getOperation();
        String		siComponentType=op.get("siComponentType", String.class);
        if ("Transformer".equals(siComponentType)) {
            return null;
        } else  {
        	return type;
        }
	}

    @Override
	protected EndPointAnalysis makeEndPoint(Frame si, int depth) {
        Operation op = si.getOperation();
        String exampleRequest = op.get("siComponentType", String.class);
        String opLabel = op.getLabel();
        EndPointName name = EndPointName.valueOf(opLabel);
        String label = name.getName();
        return new EndPointAnalysis(name, label, exampleRequest, getOperationScore(op, depth), op);
    }
}
