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
package com.springsource.insight.plugin.quartz.scheduler;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

// NOTE: class must be public so the scheduler can access it
public class QuartzSchedulerMockJob implements Job {
    public QuartzSchedulerMockJob() {
        System.out.append('\t').println(getClass().getSimpleName() + " initialized at " + new Date(System.currentTimeMillis()));
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.append('\t').println(getClass().getSimpleName() + " executing at " + new Date(System.currentTimeMillis()));
        QuartzSchedulerOperationCollectionAspectTest.SIGNALLER.release();
    }
}