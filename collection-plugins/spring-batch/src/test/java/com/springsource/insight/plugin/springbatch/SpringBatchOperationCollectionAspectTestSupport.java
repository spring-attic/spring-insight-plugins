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

import java.util.List;

import org.mockito.Mockito;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.StartLimitExceededException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.FlowExecutor;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.util.StringUtils;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

/**
 *
 */
public abstract class SpringBatchOperationCollectionAspectTestSupport
        extends OperationCollectionAspectTestSupport {
    protected SpringBatchOperationCollectionAspectTestSupport() {
        super();
    }

    @Override
    protected OperationCollector createSpiedOperationCollector(OperationCollector originalCollector) {
        return new TestDummyOperationCollector();
    }

    @Override
    protected Operation getLastEnteredOperation(OperationCollector spiedCollector) {
        assertTrue("Spied collector type mismatch", spiedCollector instanceof TestDummyOperationCollector);

        List<Operation> opsList = ((TestDummyOperationCollector) spiedCollector).getCapturedOperations();
        assertNotNull("No operations list data", opsList);
        assertTrue("No operations collected", opsList.size() > 0);
        return opsList.get(opsList.size() - 1);
    }

    protected Operation getFirstEntered() {
        OperationCollectionAspectSupport aspectInstance = getAspect();
        OperationCollector spiedCollector = aspectInstance.getCollector();
        assertTrue("Spied collector type mismatch", spiedCollector instanceof TestDummyOperationCollector);

        List<Operation> opsList = ((TestDummyOperationCollector) spiedCollector).getCapturedOperations();
        assertNotNull("No operations list data", opsList);
        assertTrue("No operations collected", opsList.size() > 0);
        return opsList.get(0);
    }

    protected Operation assertOperationDetails(Operation op, String action, String name) {
        assertNotNull("No operation extracted", op);
        assertEquals("Mismatched type value", SpringBatchDefinitions.BATCH_TYPE, op.getType());
        assertEquals("Mismatched action", action, op.get(SpringBatchDefinitions.ACTION_ATTR, String.class));
        assertEquals("Mismatched name", name, op.get(SpringBatchDefinitions.NAME_ATTR, String.class));

        SpringBatchOperationCollectionAspect aspectInstance =
                (SpringBatchOperationCollectionAspect) getAspect();
        Class<?> batchType = aspectInstance.getBatchType();
        assertEquals("Mismatched batch type", batchType.getSimpleName(), op.get(SpringBatchDefinitions.TYPE_ATTR, String.class));
        return op;
    }

    protected Operation assertOperationPath(Operation op, FlowExecutor flowExecutor) {
        return assertOperationPath(op, flowExecutor.getStepExecution());
    }

    protected Operation assertOperationPath(Operation op, StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        JobInstance jobInstance = jobExecution.getJobInstance();
        return assertOperationPath(op, jobInstance.getJobName(), stepExecution.getStepName());
    }

    protected Operation assertOperationPath(Operation op, String jobName, String stepName) {
        assertNotNull("No operation extracted", op);

        if (StringUtils.hasText(jobName)) {
            assertEquals("Mismatched job name", jobName, op.get(SpringBatchDefinitions.JOBNAME_ATTR, String.class));
        }

        if (StringUtils.hasText(stepName)) {
            assertEquals("Mismatched step name", stepName, op.get(SpringBatchDefinitions.STEPNAME_ATTR, String.class));
        }

        return op;
    }

    protected Step createTestStep(final String name) {
        return new Step() {
            public String getName() {
                return name;
            }

            public boolean isAllowStartIfComplete() {
                return false;
            }

            public int getStartLimit() {
                return 0;
            }

            public void execute(StepExecution stepExecution)
                    throws JobInterruptedException {
                System.out.println(Step.class.getSimpleName() + "#execute(" + name + ")");
            }
        };
    }

    protected FlowExecutor createFlowExecutor(String jobName, String stepName) {
        return createFlowExecutor(createStepExecution(jobName, stepName));
    }

    protected StepExecution createStepExecution(String jobName, String stepName) {
        JobExecution jobExecution = createJobExecution(jobName);
        StepExecution stepExecution = Mockito.mock(StepExecution.class);
        Mockito.when(stepExecution.getStepName()).thenReturn(stepName);
        Mockito.when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        return stepExecution;
    }

    protected JobExecution createJobExecution(String jobName) {
        JobInstance jobInstance = Mockito.mock(JobInstance.class);
        Mockito.when(jobInstance.getJobName()).thenReturn(jobName);
        JobExecution jobExecution = Mockito.mock(JobExecution.class);
        Mockito.when(jobExecution.getJobInstance()).thenReturn(jobInstance);
        return jobExecution;
    }

    protected FlowExecutor createFlowExecutor(final StepExecution stepExecution) {
        return new FlowExecutor() {
            public String executeStep(Step step)
                    throws JobInterruptedException, JobRestartException, StartLimitExceededException {
                step.execute(getStepExecution());
                return "done";
            }

            public JobExecution getJobExecution() {
                return Mockito.mock(JobExecution.class);
            }

            public StepExecution getStepExecution() {
                return stepExecution;
            }

            public void close(FlowExecution result) {
                // ignored
            }

            public void abandonStepExecution() {
                if (stepExecution != null) {
                    System.out.println("abandonStepExecution(" + stepExecution.getStepName() + ")");
                }
            }

            public void updateJobExecutionStatus(FlowExecutionStatus status) {
                // ignored
            }

            public boolean isRestart() {
                return false;
            }

            public void addExitStatus(String code) {
                // ignored
            }
        };
    }
}
