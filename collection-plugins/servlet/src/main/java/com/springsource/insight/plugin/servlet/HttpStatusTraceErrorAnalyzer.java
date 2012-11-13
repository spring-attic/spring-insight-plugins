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
package com.springsource.insight.plugin.servlet;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.AbstractTraceErrorAnalyzer;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.TraceError;
import com.springsource.insight.util.StringUtil;

public class HttpStatusTraceErrorAnalyzer extends AbstractTraceErrorAnalyzer {
    private static final TraceError contextNotAvailableError=new TraceError("Context not available");
    private static final HttpStatusTraceErrorAnalyzer	INSTANCE=new HttpStatusTraceErrorAnalyzer();
	public static final String	STATUS_CODE_ATTR="statusCode", REASON_PHRASE_ATTR="reasonPhrase";

    private HttpStatusTraceErrorAnalyzer () {
    	super(OperationType.HTTP);
    }

    public static final HttpStatusTraceErrorAnalyzer getInstance() {
    	return INSTANCE;
    }

    @Override
	public TraceError locateFrameError(Frame httpFrame) {
        Operation op = httpFrame.getOperation();
        OperationMap response = op.get("response", OperationMap.class);

        int statusCode =(response == null) ? (-1) : response.getInt(STATUS_CODE_ATTR, (-1));
        if ((statusCode >= 0) && httpStatusIsError(statusCode)) {
            return new TraceError(createErrorMessage(statusCode, response.get(REASON_PHRASE_ATTR, String.class))); 
        }

        OperationMap request = op.get("request", OperationMap.class);
        Boolean contextAvailable = (request == null) ? null : request.get(OperationFields.CONTEXT_AVAILABLE, Boolean.class);
        if ((contextAvailable != null) && (!contextAvailable.booleanValue())) {
            return contextNotAvailableError;
        } else {
        	return null;
        }
    }

	// TODO make this a general utility
	static String createErrorMessage (int statusCode, String reasonPhrase) {
		if (StringUtil.isEmpty(reasonPhrase)) {
			return String.valueOf("Status code = " + statusCode);
		} else {
			return String.valueOf(statusCode) + " " + reasonPhrase;
		}
	}
	
	// TODO make this a general utility
	static boolean httpStatusIsError(int status) {
	    return (status < 100) || (status >= 400);
	}
}
