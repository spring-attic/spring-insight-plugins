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

package com.springsource.insight.plugin.springweb.remoting;

import java.util.Collection;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.AbstractTraceErrorAnalyzer;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceError;
import com.springsource.insight.util.StringUtil;

/**
 *
 */
public class HttpInvokerRequestExecutorTraceErrorAnalyzer extends AbstractTraceErrorAnalyzer {
    private static final HttpInvokerRequestExecutorTraceErrorAnalyzer INSTANCE = new HttpInvokerRequestExecutorTraceErrorAnalyzer();

    private HttpInvokerRequestExecutorTraceErrorAnalyzer() {
        super(HttpInvokerRequestExecutorExternalResourceAnalyzer.HTTP_INVOKER);
    }

    public static final HttpInvokerRequestExecutorTraceErrorAnalyzer getInstance() {
        return INSTANCE;
    }

    @Override
    public TraceError locateFrameError(Frame frame) {
        Operation op = frame.getOperation();
        String remoteError = op.get(HttpInvokerRequestExecutorOperationCollector.REMOTE_EXCEPTION, String.class);
        if (StringUtil.isEmpty(remoteError)) {
            return null;
        } else {
            return new TraceError(remoteError);
        }
    }

    @Override    // if ANY remote invocation failed, then declare the trace an error
    public Collection<Frame> locateFrames(Trace trace) {
        return trace.getAllFramesOfType(HttpInvokerRequestExecutorExternalResourceAnalyzer.HTTP_INVOKER);
    }
}
