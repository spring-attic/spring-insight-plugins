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

package com.springsource.insight.plugin.jndi;

import java.util.Arrays;

import com.springsource.insight.intercept.endpoint.AbstractEndPointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;

/**
 * 
 */
public class JndiEndpointAnalyzer extends AbstractEndPointAnalyzer {
	public static final OperationType	LOOKUP=OperationType.valueOf("javax-naming-lookup");
	public static final OperationType	BIND=OperationType.valueOf("javax-naming-bind");
	public static final int	DEFAULT_SCORE=EndPointAnalysis.TOP_LAYER_SCORE;

	public JndiEndpointAnalyzer () {
		super(Arrays.asList(LOOKUP,BIND));
	}

	@Override
	protected int getDefaultScore(int depth) {
		return DEFAULT_SCORE;
	}

	@Override
	protected EndPointAnalysis makeEndPoint(Frame frame, int depth) {
		Operation		op=frame.getOperation();
        EndPointName 	name=EndPointName.valueOf(op);
        String 			label=op.getLabel();
        return new EndPointAnalysis(name, label, label, getOperationScore(op, depth), op);
	}
}
