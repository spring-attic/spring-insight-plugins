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
package com.springsource.insight.plugin.jcr;

import com.springsource.insight.intercept.endpoint.AbstractSingleTypeEndpointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.Frame;

public class JCREndPointAnalyzer extends AbstractSingleTypeEndpointAnalyzer {
    public JCREndPointAnalyzer() {
        super(OperationCollectionTypes.LOGIN_TYPE.type);
    }

    @Override
	protected EndPointAnalysis makeEndPoint(Frame frame, int score) {
        Operation 		op=frame.getOperation();
        
        String workspaceName=op.get("workspace", String.class);
        String repoName=op.get("repository", String.class);
        String endPointName = repoName+(workspaceName!=null?"."+workspaceName:"");
        
        return new EndPointAnalysis(EndPointName.valueOf(endPointName), "JCR: "+endPointName, endPointName, score, op);
    } 
}