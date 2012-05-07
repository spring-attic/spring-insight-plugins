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

package com.springsource.insight.plugin.springbatch;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutor;

import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public aspect FlowExecutorCollectionAspect extends SpringBatchOperationCollectionAspect {
    public FlowExecutorCollectionAspect () {
        super(FlowExecutor.class);
    }

    public pointcut collectionPoint()
        : execution(* FlowExecutor+.executeStep(Step))
       || execution(* FlowExecutor+.abandonStepExecution())
        ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Signature       sig=jp.getSignature();
        String          action=sig.getName();
        FlowExecutor    executor=(FlowExecutor) jp.getTarget();
        JobExecution    jobExecution=executor.getJobExecution();
        Operation       op;
        if ("executeStep".equals(action)) {
            Object[]    args=jp.getArgs();
            Step        step=(Step) args[0];
            String      stepName=step.getName();
            op = createOperation(jp, stepName).put(SpringBatchDefinitions.STEPNAME_ATTR, stepName);
        } else if ("abandonStepExecution".equals(action)) {
            StepExecution   stepExecution=executor.getStepExecution();
            String          stepName=(stepExecution == null) /* can happen if no current step */ ? null : stepExecution.getStepName();
            if ((stepName == null) || (stepName.length() <= 0)) {
                stepName = SpringBatchDefinitions.UNKNOWN_VALUE;
            }

            op= createOperation(jp, stepName);
            if (stepExecution != null) {
                op = fillStepExecutionDetails(op, stepExecution);
            }
        } else {
            throw new UnsupportedOperationException("Unknown action: " + sig.toString());
        }

        return fillJobExecution(op, jobExecution);
    }
}
