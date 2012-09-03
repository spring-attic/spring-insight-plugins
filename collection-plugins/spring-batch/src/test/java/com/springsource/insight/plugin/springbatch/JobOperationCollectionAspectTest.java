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
import org.springframework.batch.core.Job;

import com.springsource.insight.intercept.operation.Operation;


/**
 * 
 */
public class JobOperationCollectionAspectTest
        extends SpringBatchOperationCollectionAspectTestSupport {

    public JobOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testExecute () {
        Job     job=new TestDummyJob("testExecute");
        String  jobName=job.getName();
        job.execute(TestDummyJobRepository.createJobExecutionInstance(jobName));
        
        Operation   op=assertOperationDetails(getLastEntered(), "execute", jobName);
        assertOperationPath(op, jobName, null);
    }

    @Override
    public JobOperationCollectionAspect getAspect() {
        return JobOperationCollectionAspect.aspectOf();
    }
}
