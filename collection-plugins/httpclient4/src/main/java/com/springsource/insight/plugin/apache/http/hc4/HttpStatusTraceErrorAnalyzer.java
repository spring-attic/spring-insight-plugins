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
package com.springsource.insight.plugin.apache.http.hc4;

import java.util.Collection;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.trace.AbstractTraceErrorAnalyzer;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceError;
import com.springsource.insight.util.StringUtil;

/*
 * See servlet plugin  HttpStatusTraceErrorAnalyzer for similar logic
 */
public class HttpStatusTraceErrorAnalyzer extends AbstractTraceErrorAnalyzer {
    public static final String STATUS_CODE_ATTR = "statusCode", REASON_PHRASE_ATTR = "reasonPhrase";
    private static final HttpStatusTraceErrorAnalyzer INSTANCE = new HttpStatusTraceErrorAnalyzer();

    private HttpStatusTraceErrorAnalyzer() {
        super(HttpClientDefinitions.TYPE);
    }

    public static final HttpStatusTraceErrorAnalyzer getInstance() {
        return INSTANCE;
    }

    @Override    // if ANY invocation was an error then declare a trace error
    public Collection<Frame> locateFrames(Trace trace) {
        return trace.getAllFramesOfType(HttpClientDefinitions.TYPE);
    }

    @Override
    public TraceError locateFrameError(Frame httpFrame) {
        Operation op = (httpFrame == null) ? null : httpFrame.getOperation();
        // NOTE: if an IOException occurred we will not have a response either
        OperationMap response = (op == null) ? null : op.get("response", OperationMap.class);
        if (response == null) {
            return null;
        }

        int statusCode = response.getInt(STATUS_CODE_ATTR, (-1));
        if ((statusCode < 0) /* no code */ || (!httpStatusIsError(statusCode))) {
            return null;
        }

        String reasonPhrase = response.get(REASON_PHRASE_ATTR, String.class);
        return new TraceError(createErrorMessage(statusCode, reasonPhrase));
    }

    // TODO make this a general utility
    static String createErrorMessage(int statusCode, String reasonPhrase) {
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
