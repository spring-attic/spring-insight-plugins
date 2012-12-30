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

import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.AbstractTraceSourceAnalyzer;
import com.springsource.insight.intercept.trace.TraceSource;

public class HttpTraceSourceAnalyzer extends AbstractTraceSourceAnalyzer {
	private static final HttpTraceSourceAnalyzer INSTANCE = new HttpTraceSourceAnalyzer();
	
	private HttpTraceSourceAnalyzer() {
		super(TraceSource.HTTP, OperationType.HTTP);
	}
	
	public static final HttpTraceSourceAnalyzer getInstance() {
		return INSTANCE;
	}
}
