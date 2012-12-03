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
package com.springsource.insight.plugin.jsf;

import com.springsource.insight.intercept.endpoint.AbstractSingleTypeEndpointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameUtil;

public class JSFActionEndPointAnalyzer extends AbstractSingleTypeEndpointAnalyzer {
	public static final OperationType TYPE = OperationType.valueOf("jsf_action_listener_operation");

	private static final JSFActionEndPointAnalyzer INSTANCE = new JSFActionEndPointAnalyzer();
	
	private JSFActionEndPointAnalyzer() {
		super(TYPE);
	}
	
	public static final JSFActionEndPointAnalyzer getInstance() {
		return INSTANCE;
	}

	@Override
	protected EndPointAnalysis makeEndPoint(Frame frame, int depth) {
		Frame httpFrame = FrameUtil.getFirstParentOfType(frame, OperationType.HTTP);
		
		if (httpFrame == null|| !FrameUtil.frameIsAncestor(httpFrame, frame)) {
			return null;
		}

		Operation operation = frame.getOperation();

		String implementationClass = operation.get("implementationClass", String.class);
		String implementationClassMethod = operation.get("implementationClassMethod", String.class);
		
		String resourceKey = implementationClass + "." + implementationClassMethod;
		String resourceLabel = implementationClass + "#" + implementationClassMethod;
		
		EndPointName name = EndPointName.valueOf(resourceKey);
		Operation httpOperation = httpFrame.getOperation();
		String exampleRequest = httpOperation.getLabel();
		
		return new EndPointAnalysis(name, resourceLabel,
				exampleRequest, getOperationScore(operation, depth), operation);
	}
}
