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

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;

/**
 *
 */
public class TestDummyJob implements Job {
    private static final JobParametersValidator validator = new JobParametersValidator() {
        public void validate(JobParameters parameters)
                throws JobParametersInvalidException {
            if (parameters == null) {
                throw new JobParametersInvalidException("No parameters");
            }
        }
    };
    private static final JobParametersIncrementer incrementer = new JobParametersIncrementer() {
        public JobParameters getNext(JobParameters parameters) {
            Assert.assertNotNull("No parameters to increment", parameters);
            return parameters;
        }
    };
    private final String name;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public TestDummyJob(String jobName) {
        name = jobName;
    }

    public String getName() {
        return name;
    }

    public boolean isRestartable() {
        return false;
    }

    public void execute(JobExecution execution) {
        logger.info("execute(" + getName() + ")");
    }

    public JobParametersIncrementer getJobParametersIncrementer() {
        return incrementer;
    }

    public JobParametersValidator getJobParametersValidator() {
        return validator;
    }

}
