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
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public abstract aspect SpringBatchOperationCollectionAspect extends MethodOperationCollectionAspect {
    protected final Class<?>    batchType;

    protected SpringBatchOperationCollectionAspect (@SuppressWarnings("hiding") Class<?> batchType) {
        if ((this.batchType=batchType) == null) {
            throw new IllegalStateException("No batch type provided");
        }
    }

    public final Class<?> getBatchType () {
        return this.batchType;
    }

    protected Operation createOperation(JoinPoint jp, String name) {
        Signature   sig=jp.getSignature();
        String      action=sig.getName();
        return createOperation(jp, getBatchType(), action, name);
    }

    protected Operation createOperation(JoinPoint jp, Class<?> batchClass, String action, String name) {
        return super.createOperation(jp)
                    .type(SpringBatchDefinitions.BATCH_TYPE)
                    .label(batchClass.getSimpleName() + " " + action + " " + name)
                    .put(SpringBatchDefinitions.ACTION_ATTR, action)
                    .put(SpringBatchDefinitions.NAME_ATTR, name)
                    .put(SpringBatchDefinitions.TYPE_ATTR, batchClass.getSimpleName())
                    // these may be overridden by the specific aspects - we only ensure non-null value
                    .put(SpringBatchDefinitions.JOBNAME_ATTR, SpringBatchDefinitions.UNKNOWN_VALUE)
                    .put(SpringBatchDefinitions.STEPNAME_ATTR, SpringBatchDefinitions.UNKNOWN_VALUE)
                    ;
    }

    protected Operation fillStepExecutionDetails (Operation op, StepExecution stepExecution) {
        if (stepExecution == null) {
            return op;
        }

        return fillJobExecution(op, stepExecution.getJobExecution())
                 .put(SpringBatchDefinitions.STEPNAME_ATTR, stepExecution.getStepName())
                 ;
    }

    protected Operation fillJobExecution (Operation op, JobExecution jobExecution) {
        if (jobExecution == null) {
            return op;
        }

        return fillJobJobInstance(op, jobExecution.getJobInstance());
    }

    protected Operation fillJobJobInstance(Operation op,JobInstance jobInstance) {
        if (jobInstance == null) {
            return op;
        }

        return op.put(SpringBatchDefinitions.JOBNAME_ATTR, jobInstance.getJobName());
    }

    @Override
    public boolean isEndpoint() {
        return true;
    }
    
    @Override
    public String getPluginName() {
        return SpringBatchPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
