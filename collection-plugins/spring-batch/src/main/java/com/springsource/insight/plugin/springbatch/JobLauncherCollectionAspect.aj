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
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import com.springsource.insight.intercept.operation.Operation;

/**
 *
 */
public aspect JobLauncherCollectionAspect extends SpringBatchOperationCollectionAspect {
    public JobLauncherCollectionAspect() {
        super(JobLauncher.class);
    }

    public pointcut collectionPoint(): execution(* JobLauncher+.run(Job,JobParameters));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[] args = jp.getArgs();
        Job job = (Job) args[0];
        String jobName = job.getName();

        return createOperation(jp, jobName)
                .put(SpringBatchDefinitions.JOBNAME_ATTR, jobName)
                ;
    }
}
