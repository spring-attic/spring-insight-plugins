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

import com.springsource.insight.intercept.endpoint.AbstractSingleTypeEndpointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.util.StringUtil;

/**
 * Locates servlet endpoints within Traces.  
 * 
 * If an HTTP {@link Operation} is detected, an endpoint analysis will be returned.
 * The score of the analysis will always be 0, and its endpoint key/label
 * will be based on the servlet's name.
 */
public class ServletEndPointAnalyzer extends AbstractSingleTypeEndpointAnalyzer {
    public static final int ANALYSIS_SCORE = EndPointAnalysis.TOP_LAYER_SCORE;

    public ServletEndPointAnalyzer () {
    	super(OperationType.HTTP);
    }

    @Override
    protected int getDefaultScore(int depth) {
    	return ANALYSIS_SCORE;
    }

    @Override
    protected EndPointAnalysis makeEndPoint(Frame httpFrame, int depth) {
        Operation op = httpFrame.getOperation();
        OperationMap request = op.get("request", OperationMap.class);
        
        String 	servletName=(request == null) ? null : request.get("servletName", String.class);
        String 	endPointKey=sanitizeEndPointKey(servletName);
        String 	endPointLabel = "Servlet: " + servletName;
        String	example=EndPointAnalysis.createHttpExampleRequest(request);
        if (StringUtil.isEmpty(example)) {
        	example = op.getLabel();
        }

        return new EndPointAnalysis(EndPointName.valueOf(endPointKey), endPointLabel, example, getOperationScore(op, depth), op);
    }

    static String sanitizeEndPointKey(String endPointKey) {
    	if (StringUtil.isEmpty(endPointKey)) {
    		return "unknonwn-servlet-name";
    	} else {
    		return endPointKey.replace('/', '_');
    	}
    }

    static String getExampleRequest(Operation op) {
        OperationMap details = op.get("request", OperationMap.class);
        return ((details == null) ? "???" : String.valueOf(details.get("method")))
             + " " + ((details == null) ? "<UNKNOWN>" : details.get(OperationFields.URI));
    }
}
