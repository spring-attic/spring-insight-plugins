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

import org.junit.Test;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.StartLimitExceededException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutor;
import org.springframework.batch.core.repository.JobRestartException;

import com.springsource.insight.intercept.operation.Operation;


/**
 *
 */
public class FlowExecutorCollectionAspectTest
        extends SpringBatchOperationCollectionAspectTestSupport {

    public FlowExecutorCollectionAspectTest() {
        super();
    }

    @Test
    public void testExecuteStep()
            throws JobInterruptedException, JobRestartException, StartLimitExceededException {
        Step step = createTestStep("testExecuteStep");
        String stepName = step.getName();
        FlowExecutor executor = createFlowExecutor("testExecuteJob", stepName);

        executor.executeStep(step);

        Operation op = assertOperationDetails(getFirstEntered(), "executeStep", step.getName());
        assertOperationPath(op, null, stepName);
    }

    @Test
    public void testAbandonStepExecution() {
        StepExecution stepExec = createStepExecution("testAbandonJobExecution", "testAbandonStepExecution");
        FlowExecutor executor = createFlowExecutor(stepExec);
        executor.abandonStepExecution();

        Operation op = assertOperationDetails(getFirstEntered(), "abandonStepExecution", stepExec.getStepName());
        assertOperationPath(op, stepExec);
    }

    @Test
    public void testAbandonNoCurrentStepExecution() {
        FlowExecutor executor = createFlowExecutor(null);
        executor.abandonStepExecution();
        assertOperationDetails(getFirstEntered(), "abandonStepExecution", SpringBatchDefinitions.UNKNOWN_VALUE);
    }

    @Override
    public FlowExecutorCollectionAspect getAspect() {
        return FlowExecutorCollectionAspect.aspectOf();
    }

}
