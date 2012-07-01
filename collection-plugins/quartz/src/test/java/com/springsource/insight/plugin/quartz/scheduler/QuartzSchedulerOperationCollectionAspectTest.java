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
package com.springsource.insight.plugin.quartz.scheduler;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.utils.Key;

import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public class QuartzSchedulerOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
    static final Semaphore  SIGNALLER=new Semaphore(0, true);
    public QuartzSchedulerOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testCollectionPoint () throws SchedulerException, InterruptedException {
        SchedulerFactory    sf = new StdSchedulerFactory();
        Scheduler           sched = sf.getScheduler();
        
        Trigger trigger = TriggerBuilder.newTrigger()
        				.withIdentity("testCollectionPoint").startNow()
        	    		.withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0).withIntervalInMilliseconds(125L))
        	    		.build();
        
        JobDetail jobDetail = JobBuilder.newJob(QuartzSchedulerMockJob.class).withIdentity("testCollectionPointJob", "testCollectionPointGroup").build();

        sched.scheduleJob(jobDetail, trigger);
        sched.start();
        // let the program have an opportunity to run the job
        Assert.assertTrue("No signal from job", SIGNALLER.tryAcquire(5L, TimeUnit.SECONDS));
        sched.shutdown(true);

        Operation   op=getLastEntered();
        Assert.assertNotNull("No operation extracted", op);
        Assert.assertEquals("Mismatched type", QuartzSchedulerDefinitions.TYPE, op.getType());
        assertJobDetails(op, jobDetail);
    }

    @Override
    public QuartzSchedulerOperationCollectionAspect getAspect() {
        return QuartzSchedulerOperationCollectionAspect.aspectOf();
    }

    private static void assertJobDetails (Operation op, JobDetail detail) {
    	Key<?> jobKey=detail.getKey();
        assertOperationValue(op, "name", jobKey.getName());
        assertOperationValue(op, "group", jobKey.getGroup());
        assertOperationValue(op, "fullName", jobKey.getGroup()+"."+jobKey.getName());
        assertOperationValue(op, "description", detail.getDescription());
        assertOperationValue(op, "jobClass", detail.getJobClass().getName());
    }

    private static void assertOperationValue (Operation op, String key, Object expected) {
        Assert.assertEquals("Mismatched " + key + " value", expected, op.get(key));
    }
}
