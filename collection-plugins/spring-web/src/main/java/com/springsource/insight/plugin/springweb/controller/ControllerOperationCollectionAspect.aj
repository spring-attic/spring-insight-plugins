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

package com.springsource.insight.plugin.springweb.controller;

import org.aspectj.lang.JoinPoint;
import org.springframework.web.bind.annotation.RequestMapping;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;

public aspect ControllerOperationCollectionAspect extends AbstractControllerOperationCollectionAspect {
    /**
     * Name of the {@link OperationMap} used to hold any model argument found
     * in the invocation
     */
    public static final String MODEL_ARGUMENT_NAME = "modelArgument";

    public ControllerOperationCollectionAspect() {
        super(false);
    }

    public pointcut collectionPoint(): execution(@RequestMapping * *(..));

    @Override
    public Operation createOperation(JoinPoint jp) {
        Operation op = super.createOperation(jp);
        if (ControllerOperationCollector.collectExtraInformation()) {
            ControllerOperationCollector.collectModelInformation(op, MODEL_ARGUMENT_NAME, jp.getArgs());
        }

        return op;
    }
}
