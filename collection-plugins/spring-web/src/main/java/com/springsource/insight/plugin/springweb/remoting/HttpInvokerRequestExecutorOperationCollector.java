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


import org.springframework.remoting.support.RemoteInvocationResult;

import com.springsource.insight.collection.DefaultOperationCollector;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.util.StringFormatterUtils;

/**
 *
 */
public class HttpInvokerRequestExecutorOperationCollector extends DefaultOperationCollector {
    public static final String REMOTE_EXCEPTION = "remoteException";

    HttpInvokerRequestExecutorOperationCollector() {
        super();
    }

    @Override
    protected void processNormalExit(Operation op, Object returnValue) {
        RemoteInvocationResult result = (RemoteInvocationResult) returnValue;
        Throwable remoteError = result.getException();
        if (remoteError != null) {
            op.put(REMOTE_EXCEPTION, StringFormatterUtils.formatStackTrace(remoteError));
        } else {
            op.put(OperationFields.RETURN_VALUE, StringFormatterUtils.formatObject(result.getValue()));
        }
    }
}
