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

package com.springsource.insight.plugin.springweb.binder;

import org.aspectj.lang.JoinPoint;
import org.springframework.web.bind.annotation.InitBinder;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.plugin.springweb.AbstractSpringWebAspectSupport;

public aspect InitBinderOperationCollectionAspect extends AbstractSpringWebAspectSupport {
    public static final OperationType TYPE = OperationType.valueOf("init_binder");
    private InitBinderOperationFinalizer finalizer;

    public InitBinderOperationCollectionAspect() {
        finalizer = InitBinderOperationFinalizer.getInstance();
    }

    public pointcut collectionPoint(): execution(@InitBinder * *(..));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        return finalizer.registerWithSelf(new Operation().type(TYPE).sourceCodeLocation(getSourceCodeLocation(jp)), jp);
    }
}
