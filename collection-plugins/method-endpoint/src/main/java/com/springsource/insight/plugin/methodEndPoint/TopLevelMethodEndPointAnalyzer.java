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

package com.springsource.insight.plugin.methodEndPoint;

import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;

/**
 * If a MethodOperation is a root-level operation within a Trace, create
 * an EndPoint for it.
 * 
 * A MethodOperation can appear at the root level whenever a method is
 * invoked asynchronously from a web request (such as a background thread,
 * scheduled threads, etc.)
 */
public class TopLevelMethodEndPointAnalyzer implements EndPointAnalyzer {
    
    private static final OperationType TYPE = OperationType.METHOD;

    public EndPointAnalysis locateEndPoint(Trace trace) {
    	Frame root = trace.getRootFrame();
    	if (root == null) {
    		return null;
    	}
    	
    	Operation rootOp = root.getOperation();
        if (!rootOp.getType().equals(TYPE)) {
        	return null;
    	}
        
        EndPointName name = EndPointName.valueOf(rootOp);
        String label = rootOp.getLabel();
        String exampleRequest = "";
        OperationList args = rootOp.get(OperationFields.ARGUMENTS, OperationList.class);
        if (args.size() == 1) {
        	exampleRequest = args.get(0, String.class); 
        }
        int score = 0;
        return new EndPointAnalysis(name, label, exampleRequest, score);
    }
}
