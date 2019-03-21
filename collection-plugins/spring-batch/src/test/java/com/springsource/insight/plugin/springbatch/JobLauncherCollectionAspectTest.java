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

import org.junit.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;

import com.springsource.insight.intercept.operation.Operation;


/**
 *
 */
public class JobLauncherCollectionAspectTest
        extends SpringBatchOperationCollectionAspectTestSupport {

    public JobLauncherCollectionAspectTest() {
        super();
    }

    @Test
    public void testRunJob() throws Exception {
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(new TestDummyJobRepository());
        launcher.afterPropertiesSet();

        Job job = new TestDummyJob("testRunJob");
        JobExecution execution = launcher.run(job, new JobParameters());
        assertNotNull("No job exectuion instance", execution);

        String jobName = job.getName();
        Operation op = assertOperationDetails(getFirstEntered(), "run", job.getName());
        assertOperationPath(op, jobName, null);
    }

    @Override
    public JobLauncherCollectionAspect getAspect() {
        return JobLauncherCollectionAspect.aspectOf();
    }

}
