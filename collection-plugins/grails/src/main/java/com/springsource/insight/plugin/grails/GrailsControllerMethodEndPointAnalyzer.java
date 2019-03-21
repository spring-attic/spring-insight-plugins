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

package com.springsource.insight.plugin.grails;

import com.springsource.insight.intercept.endpoint.AbstractSingleTypeEndpointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.operation.SourceCodeLocation;
import com.springsource.insight.intercept.trace.Frame;

public class GrailsControllerMethodEndPointAnalyzer extends AbstractSingleTypeEndpointAnalyzer {
    public static final OperationType TYPE = OperationType.valueOf("grails_controller_method");
    private static final GrailsControllerMethodEndPointAnalyzer	INSTANCE=new GrailsControllerMethodEndPointAnalyzer();

    private GrailsControllerMethodEndPointAnalyzer () {
    	super(TYPE);
    }

    public static final GrailsControllerMethodEndPointAnalyzer getInstance() {
    	return INSTANCE;
    }

	@Override
	protected EndPointAnalysis makeEndPoint(Frame grailsFrame, int depth) {
        Operation operation = grailsFrame.getOperation();
        String resourceKey = makeResourceKey(operation.getSourceCodeLocation());
        String resourceLabel = operation.getLabel();
        String exampleRequest = EndPointAnalysis.getHttpExampleRequest(grailsFrame);
        return new EndPointAnalysis(EndPointName.valueOf(resourceKey),
                                    resourceLabel,
                                    exampleRequest,
                                    getOperationScore(operation, depth),
                                    operation);
    }

	static String makeResourceKey (SourceCodeLocation actionLocation) {
		return actionLocation.getClassName() + "." + actionLocation.getMethodName();
	}
}
