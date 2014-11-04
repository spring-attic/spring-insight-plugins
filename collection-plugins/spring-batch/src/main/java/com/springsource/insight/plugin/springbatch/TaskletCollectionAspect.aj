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

package com.springsource.insight.plugin.springbatch;

import org.aspectj.lang.JoinPoint;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;

import com.springsource.insight.intercept.operation.Operation;

/**
 *
 */
public aspect TaskletCollectionAspect extends SpringBatchOperationCollectionAspect {
    public TaskletCollectionAspect() {
        super(Tasklet.class);
    }

    public pointcut collectionPoint(): execution(* Tasklet+.execute(StepContribution,ChunkContext));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[] args = jp.getArgs();
        ChunkContext chunkContext = (ChunkContext) args[1];
        StepContext stepContext = chunkContext.getStepContext();
        StepExecution stepExecution = stepContext.getStepExecution();
        String stepName = stepExecution.getStepName();
        return fillStepExecutionDetails(createOperation(jp, stepName), stepExecution);
    }

}
