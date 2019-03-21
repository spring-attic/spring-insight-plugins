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
package com.springsource.insight.plugin.quartz.scheduler;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;

/**
 * 
 */
public class QuartzSchedulerOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
    static final Semaphore  SIGNALLER=new Semaphore(0, true);
    private static final QuartzKeyValueAccessor	keyAccessor=QuartzKeyValueAccessor.getInstance();

    public QuartzSchedulerOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testCollectionPoint () throws SchedulerException, InterruptedException {
        SchedulerFactory    sf=new StdSchedulerFactory();
        Scheduler           sched=sf.getScheduler();
        
        Trigger trigger = TriggerBuilder.newTrigger()
        				.withIdentity("testCollectionPointTrigger", "testCollectionPointTriggerGroup").startNow()
        	    		.withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0).withIntervalInMilliseconds(125L))
        	    		.build();
        
        JobDetail jobDetail = JobBuilder.newJob(QuartzSchedulerMockJob.class).withIdentity("testCollectionPointJob", "testCollectionPointJobGroup").build();

        sched.scheduleJob(jobDetail, trigger);
        sched.start();
        // let the program have an opportunity to run the job
        assertTrue("No signal from job", SIGNALLER.tryAcquire(5L, TimeUnit.SECONDS));
        sched.shutdown(true);

        Operation   op=getLastEntered();
        assertNotNull("No operation extracted", op);
        assertEquals("Mismatched type", QuartzSchedulerDefinitions.TYPE, op.getType());

        assertJobDetails(op, jobDetail);
        assertTriggerDetails(op, trigger);
    }

    @Override
    public QuartzSchedulerOperationCollectionAspect getAspect() {
        return QuartzSchedulerOperationCollectionAspect.aspectOf();
    }

    private static OperationMap assertTriggerDetails (Operation op, Trigger trigger) {
    	return assertTriggerDetails((op == null) ? null : op.get("trigger", OperationMap.class), trigger);
    }

    private static OperationMap assertTriggerDetails (OperationMap map, Trigger trigger) {
    	assertNotNull("No trigger details", map);
    	assertKeyValue(map, trigger.getKey());
    	assertEquals("Mismatched priority value", trigger.getPriority(), map.getInt("priority", (-1)));
    	assertOperationStringValue(map, "description", trigger.getDescription());
    	assertOperationStringValue(map, "calendarName", trigger.getCalendarName());
    	return map;
    }

    private static void assertKeyValue (OperationMap map, Object key) {
        assertOperationStringValue(map, "name", keyAccessor.getName(key));
        assertOperationStringValue(map, "group", keyAccessor.getGroup(key));
        assertOperationStringValue(map, "fullName", keyAccessor.getFullName(key));
    }

    private static void assertOperationStringValue (OperationMap op, String key, String expected) {
        assertEquals("Mismatched map=" + key + " value", expected, op.get(key, String.class));
    }

    private static Operation assertJobDetails (Operation op, JobDetail detail) {
        assertNotNull("No operation extracted", op);

        assertKeyValue(op, detail.getKey());
        assertOperationStringValue(op, "description", detail.getDescription());
        assertOperationStringValue(op, "jobClass", detail.getJobClass().getName());
        return op;
    }

    private static void assertKeyValue (Operation op, Object key) {
        assertOperationStringValue(op, "name", keyAccessor.getName(key));
        assertOperationStringValue(op, "group", keyAccessor.getGroup(key));
        assertOperationStringValue(op, "fullName", keyAccessor.getFullName(key));
    }

    private static void assertOperationStringValue (Operation op, String key, String expected) {
        assertEquals("Mismatched op=" + key + " value", expected, op.get(key, String.class));
    }
}
