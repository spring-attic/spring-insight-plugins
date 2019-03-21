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

package com.springsource.insight.plugin.springcore;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.util.ArrayUtil;


/**
 *
 */
public abstract aspect SpringLifecycleMethodOperationCollectionAspect
        extends SpringCoreOperationCollectionAspect {
    public static final String EVENT_ATTR = "eventInfo";
    protected final OperationType operationType;

    protected SpringLifecycleMethodOperationCollectionAspect(OperationType opType) {
        if ((operationType = opType) == null) {
            throw new IllegalStateException("No operation type provided");
        }
    }

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Operation op = super.createOperation(jp)
                .type(operationType);
        return updateEventData(op, jp);
    }

    protected Operation updateEventData(Operation op, JoinPoint jp) {
        return updateEventDataFromArgs(op, jp.getArgs());
    }

    protected Operation updateEventDataFromArgs(Operation op, Object... args) {
        return updateEventData(op, (ArrayUtil.length(args) <= 0) ? null : args[0]);
    }

    protected Operation updateEventData(Operation op, Object event) {
        return op.putAnyNonEmpty(EVENT_ATTR, resolveEventData(event));
    }

    protected abstract String resolveEventData(Object event);
}
