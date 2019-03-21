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

package com.springsource.insight.plugin.springbatch;

import org.aspectj.lang.JoinPoint;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;

import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public aspect StepOperationCollectionAspect extends SpringBatchOperationCollectionAspect {
    public StepOperationCollectionAspect () {
        super(Step.class);
    }
    
    public pointcut collectionPoint() : execution(* Step+.execute(StepExecution));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Step            step=(Step) jp.getTarget();
        String          stepName=step.getName();
        Object[]        args=jp.getArgs();
        return fillStepExecutionDetails(createOperation(jp, stepName), (StepExecution) args[0]);
    }
}
