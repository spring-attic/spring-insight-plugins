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

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;

/**
 *
 */
public class TestDummyJobRepository implements JobRepository {
    private final Map<String, JobExecution> execsMap = new TreeMap<String, JobExecution>();
    private static final AtomicLong idsGenerator = new AtomicLong(1L);

    public TestDummyJobRepository() {
        super();
    }

    public boolean isJobInstanceExists(String jobName, JobParameters jobParameters) {
        return execsMap.containsKey(jobName);
    }

    public JobInstance createJobInstance(String s, JobParameters jobParameters) {
        return null;
    }

    public JobExecution createJobExecution(JobInstance jobInstance, JobParameters jobParameters, String s) {
        return null;
    }

    public JobExecution createJobExecution(String jobName, JobParameters jobParameters)
            throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        if (isJobInstanceExists(jobName, jobParameters)) {
            throw new JobExecutionAlreadyRunningException("Job already executing");
        }

        return createJobExecutionInstance(jobName, jobParameters);
    }

    public void update(JobExecution jobExecution) {
        BatchStatus status = jobExecution.getStatus();
        JobInstance instance = jobExecution.getJobInstance();
        String jobName = instance.getJobName();
        if (BatchStatus.STARTING.equals(status)) {
            Assert.assertNull("Multiple executions for job=" + jobName, execsMap.put(jobName, jobExecution));
        } else if (BatchStatus.COMPLETED.equals(status)
                || BatchStatus.ABANDONED.equals(status)
                || BatchStatus.FAILED.equals(status)) {
            Assert.assertNotNull("No running execution for job=" + jobName, execsMap.remove(jobName));
        }
    }

    public void add(StepExecution stepExecution) {
        // ignored
    }

    public void addAll(Collection<StepExecution> stepExecutions) {

    }

    public void update(StepExecution stepExecution) {
        // ignored
    }

    public void updateExecutionContext(StepExecution stepExecution) {
        // ignored
    }

    public void updateExecutionContext(JobExecution jobExecution) {
        // ignored

    }

    public StepExecution getLastStepExecution(JobInstance jobInstance, String stepName) {
        return null;
    }

    public int getStepExecutionCount(JobInstance jobInstance, String stepName) {
        return 0;
    }

    public JobExecution getLastJobExecution(String jobName, JobParameters jobParameters) {
        return execsMap.get(jobName);
    }

    static JobExecution createJobExecutionInstance(String jobName) {
        return createJobExecutionInstance(jobName, new JobParameters());
    }

    static JobExecution createJobExecutionInstance(String jobName, JobParameters jobParameters) {
        Assert.assertNotNull("No job name specified", jobName);
        Assert.assertFalse("Empty job name", jobName.length() <= 0);
        Assert.assertNotNull("No job parameters provided", jobParameters);

        Long id = Long.valueOf(idsGenerator.incrementAndGet());
        JobInstance instance = new JobInstance(id, jobName);
        return new JobExecution(instance, jobParameters);
    }
}
