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
package com.springsource.insight.plugin.springcloud.hystrix;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.AbstractTraceErrorAnalyzer;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.TraceError;
import com.springsource.insight.plugin.springcloud.SpringCloudPluginRuntimeDescriptor;
import org.apache.commons.lang.StringUtils;


public class HystrixCommandErrorAnalyzer extends AbstractTraceErrorAnalyzer {

    protected HystrixCommandErrorAnalyzer(OperationType opType) {
        super(opType);
    }

    private static final HystrixCommandErrorAnalyzer INSTANCE = new HystrixCommandErrorAnalyzer();
    private HystrixCommandErrorAnalyzer() {
        super(SpringCloudPluginRuntimeDescriptor.HYSTRIX_COMMAND);
    }
    public static final HystrixCommandErrorAnalyzer getInstance() {
        return INSTANCE;
    }

    @Override
    public TraceError locateFrameError(Frame frame) {
        Operation operation = frame.getOperation();
        if (operation != null && operation.getType() == SpringCloudPluginRuntimeDescriptor.HYSTRIX_COMMAND) {
            String events = operation.get("events", String.class);
            if (!StringUtils.isEmpty(events)) {
                String[] eventArr = events.split(",");
                for(int i = 0; i < eventArr.length; i++) {
                    String event = eventArr[i];
                    if (isErrorEvent(event.trim())) {
                        return new TraceError("HystrixCommand failed: " + event);
                    }
                }
            }
        }
        return null;
    }


    private boolean isErrorEvent(String event) {
        if ("TIMEOUT".equals(event))
            return true;
        if ("FAILURE".equals(event))
            return true;
        if ("SHORT_CIRCUITED".equals(event))
            return true;
        if ("THREAD_POOL_REJECTED".equals(event))
            return true;
        if ("SEMAPHORE_REJECTED".equals(event))
            return true;
        if ("FALLBACK_FAILURE".equals(event))
            return true;
        if ("FALLBACK_REJECTION".equals(event))
            return true;
        if ("EXCEPTION_THROWN".equals(event))
            return true;
        if ("BAD_REQUEST".equals(event))
            return true;
        return false;

    }

}
