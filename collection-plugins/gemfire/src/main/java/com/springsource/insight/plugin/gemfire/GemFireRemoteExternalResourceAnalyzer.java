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

package com.springsource.insight.plugin.gemfire;


import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.util.StringUtil;

public class GemFireRemoteExternalResourceAnalyzer extends AbstractGemFireExternalResourceAnalyzer {
	private static final GemFireRemoteExternalResourceAnalyzer	INSTANCE=new GemFireRemoteExternalResourceAnalyzer();

	private GemFireRemoteExternalResourceAnalyzer () {
		super(GemFireDefenitions.TYPE_REMOTE.getType());
	}

	public static final GemFireRemoteExternalResourceAnalyzer getInstance() {
		return INSTANCE;
	}

	@Override
    protected String getHostname(Operation op) {
	    return op.get(GemFireDefenitions.FIELD_HOST, String.class);
	}
	
	@Override
    protected int getPort(Operation op) {
	    Number portObj = op.get(GemFireDefenitions.FIELD_PORT, Number.class);
        int port = portObj != null ? portObj.intValue() : EMPTY_PORT;
        
        return port;
	}
	
	@Override
    protected boolean shouldCreateExteranlResource(Frame frame) {
	    Operation op = frame.getOperation();
	    String hostname = getHostname(op);
	    return !StringUtil.isEmpty(hostname) && !GemFireDefenitions.FIELD_UNKNOWN.equals(hostname);
	}
}
