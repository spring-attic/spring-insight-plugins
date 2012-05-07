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

package com.springsource.insight.plugin.grails;

import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.operation.SourceCodeLocation;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameUtil;
import com.springsource.insight.intercept.trace.Trace;

public class GrailsControllerMethodEndPointAnalyzer implements EndPointAnalyzer {
    
    private static final OperationType TYPE = OperationType.valueOf("grails_controller_method");
    
    public EndPointAnalysis locateEndPoint(Trace trace) {
        Frame grailsFrame = trace.getFirstFrameOfType(TYPE);
        if (grailsFrame == null) {
            return null;
        }
        
        Frame httpFrame = trace.getFirstFrameOfType(OperationType.HTTP);
        if ((httpFrame == null) || (!FrameUtil.frameIsAncestor(httpFrame, grailsFrame))) {
            return null;
        }
        
        Operation operation = grailsFrame.getOperation();
        SourceCodeLocation actionLocation = operation.getSourceCodeLocation();
        
        String resourceKey = actionLocation.getClassName() + "." + actionLocation.getMethodName();
        String resourceLabel = operation.getLabel();
        
        Operation httpOperation = httpFrame.getOperation();
        String exampleRequest = httpOperation.getLabel();
        int score = FrameUtil.getDepth(grailsFrame);        
        return new EndPointAnalysis(EndPointName.valueOf(resourceKey),
                                    resourceLabel,
                                    exampleRequest, score); 
    }
}
