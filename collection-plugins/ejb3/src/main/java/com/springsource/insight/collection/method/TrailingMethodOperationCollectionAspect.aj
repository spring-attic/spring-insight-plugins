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
package com.springsource.insight.collection.method;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.collection.TrailingAbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;

/**
 * Performs a similar functionality like MethodOperationCollectionAspect, but
 * allows for the possibility that the generated {@link Operation} may be
 * eventually discarded 
 */
public abstract aspect TrailingMethodOperationCollectionAspect
        extends TrailingAbstractOperationCollectionAspect {
    protected TrailingMethodOperationCollectionAspect () {
        super();
    }

    public TrailingMethodOperationCollectionAspect(OperationCollector operationCollector) {
        super(operationCollector);
    }

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Operation op = new Operation();
        JoinPointFinalizer.register(op,  jp);
        return op;
    }

    @Override
    public String getPluginName() {
        return "ebj3";
    }
}
