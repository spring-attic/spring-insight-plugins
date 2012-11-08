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

package com.springsource.insight.plugin.springweb.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceError;
import com.springsource.insight.intercept.trace.TraceErrorAnalyzer;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;

/**
 * 
 */
public class ClientHttpRequestTraceErrorAnalyzer implements TraceErrorAnalyzer {
	public static final String	STATUS_CODE_ATTR="statusCode", REASON_PHRASE_ATTR="reasonPhrase";
	private static final ClientHttpRequestTraceErrorAnalyzer	INSTANCE=new ClientHttpRequestTraceErrorAnalyzer();

	private ClientHttpRequestTraceErrorAnalyzer() {
		super();
	}

	public static final ClientHttpRequestTraceErrorAnalyzer getInstance() {
		return INSTANCE;
	}

	public List<TraceError> locateErrors(Trace trace) {
		Collection<Frame>	frames=trace.getAllFramesOfType(ClientHttpRequestOperationCollector.TYPE);
		if (ListUtil.size(frames) <= 0) {
			return Collections.emptyList();
		}

		List<TraceError>	errors=null;
		for (Frame frame : frames) {
			Operation		op=frame.getOperation();
			OperationMap	rsp=op.get("response", OperationMap.class);
			if (rsp == null) {
				continue;
			}

			Number	statusCode=rsp.get(STATUS_CODE_ATTR, Number.class);
			if ((statusCode == null) || (!httpStatusIsError(statusCode.intValue()))) {
				continue;
			}
			
			String	reasonPhrase=rsp.get(REASON_PHRASE_ATTR, String.class);
			if (errors == null) {
				errors = new ArrayList<TraceError>(frames.size());
			}
			
			errors.add(new TraceError(createErrorMessage(statusCode.intValue(), reasonPhrase)));
		}
		
		if (errors == null) {
			return Collections.emptyList();
		} else {
			return errors;
		}
	}

	static String createErrorMessage (int statusCode, String reasonPhrase) {
		if (StringUtil.isEmpty(reasonPhrase)) {
			return String.valueOf(statusCode);
		} else {
			return String.valueOf(statusCode) + " " + reasonPhrase;
		}
	}

    static boolean httpStatusIsError(int status) {
        return (status < 100) || (status >= 400);
    }
}
