/**
 * Copyright 2009-2011 the original author or authors.
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

package com.springsource.insight.plugin.methodEndPoint;

import static com.springsource.insight.intercept.operation.OperationFields.EXCEPTION;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.List;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceError;
import com.springsource.insight.intercept.trace.TraceErrorAnalyzer;

/**
 * If a top-level operation within a Trace contains a value 
 * for {@link OperationFields#EXCEPTION}, uses a sample of it
 * and returns the error. 
 */
public class TopLevelMethodErrorAnalyzer implements TraceErrorAnalyzer {
    public static final int MAX_ERROR_LENGTH = 80;
    
    public List<TraceError> locateErrors(Trace trace) {
    	Frame root = trace.getRootFrame();
    	if (root == null) {
    		return emptyList();
    	}
    	
    	Operation rootOp = root.getOperation();
        String exception = rootOp.get(EXCEPTION, String.class);
        if (exception != null) {
            int chars = Math.min(exception.length(), MAX_ERROR_LENGTH);
            return singletonList(new TraceError(exception.substring(0, chars)));
        }
        return emptyList();
    }
}
