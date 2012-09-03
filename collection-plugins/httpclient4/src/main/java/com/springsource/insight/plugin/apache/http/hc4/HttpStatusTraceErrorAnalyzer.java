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
package com.springsource.insight.plugin.apache.http.hc4;

import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceError;
import com.springsource.insight.intercept.trace.TraceErrorAnalyzer;

/*
 * See servlet plugin  HttpStatusTraceErrorAnalyzer for similar logic
 */
public class HttpStatusTraceErrorAnalyzer implements TraceErrorAnalyzer {
    public HttpStatusTraceErrorAnalyzer () {
        super();
    }
    /*
     * @see com.springsource.insight.intercept.trace.TraceErrorAnalyzer#locateErrors(com.springsource.insight.intercept.trace.Trace)
     */
    public List<TraceError> locateErrors (Trace trace) {
        Frame httpFrame = trace.getFirstFrameOfType(HttpClientDefinitions.TYPE);
        Operation op = (httpFrame == null) ? null : httpFrame.getOperation();
        // NOTE: if an IOException occurred we will not have a response either
        OperationMap response = (op == null) ? null : op.get("response", OperationMap.class);
        if (response == null) {
            return Collections.emptyList();
        }

        int statusCode = response.get("statusCode", Integer.class).intValue();
        if (httpStatusIsError(statusCode)) {
            return Collections.singletonList(new TraceError("Status code = " + statusCode)); 
        }

        return Collections.emptyList();
    }

    boolean httpStatusIsError(int status) {
        return status >= 500 && status < 600;
    }

}
