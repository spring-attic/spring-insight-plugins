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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceError;
import com.springsource.insight.intercept.trace.TraceErrorAnalyzer;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;

/**
 * 
 */
public class HttpInvokerRequestExecutorTraceErrorAnalyzer implements TraceErrorAnalyzer {
	private static final HttpInvokerRequestExecutorTraceErrorAnalyzer	INSTANCE=new HttpInvokerRequestExecutorTraceErrorAnalyzer();

	private HttpInvokerRequestExecutorTraceErrorAnalyzer() {
		super();
	}

	public static final HttpInvokerRequestExecutorTraceErrorAnalyzer getInstance() {
		return INSTANCE;
	}

	public List<TraceError> locateErrors(Trace trace) {
		Collection<Frame>	frames=trace.getAllFramesOfType(HttpInvokerRequestExecutorExternalResourceAnalyzer.HTTP_INVOKER);
		if (ListUtil.size(frames) <= 0) {
			return Collections.emptyList();
		}

		List<TraceError>	errors=null;
		for (Frame frame : frames) {
			Operation	op=frame.getOperation();
			String		remoteError=op.get(HttpInvokerRequestExecutorOperationCollector.REMOTE_EXCEPTION, String.class);
			if (StringUtil.isEmpty(remoteError)) {
				continue;
			}

			if (errors == null) {
				errors = new ArrayList<TraceError>(frames.size());
			}
			
			errors.add(new TraceError(remoteError));
		}
		
		if (errors == null) {
			return Collections.emptyList();
		} else {
			return errors;
		}
	}

}
