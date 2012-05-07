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

import java.util.Date;

import org.aspectj.lang.JoinPoint;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.trace.FrameBuilder;

/**
 * 
 */
public aspect QuartzSchedulerOperationCollectionAspect extends MethodOperationCollectionAspect {
    private static final InterceptConfiguration configuration = InterceptConfiguration.getInstance();

    public pointcut collectionPoint()
        : execution(* org.quartz.Job.execute(JobExecutionContext))
        ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Operation   op=super.createOperation(jp)
                        .type(QuartzSchedulerDefinitions.TYPE);
        return createOperation(op, (JobExecutionContext) jp.getArgs()[0]);
    }

    Operation createOperation (Operation op, JobExecutionContext context) {
        op.putAnyNonEmpty("fireTime", safeCloneDate(context.getFireTime()))
          .put("refireCount", context.getRefireCount())
          .putAnyNonEmpty("scheduledFireTime", safeCloneDate(context.getScheduledFireTime()))
          .putAnyNonEmpty("previousFireTime", safeCloneDate(context.getPreviousFireTime()))
          .putAnyNonEmpty("nextFireTime", safeCloneDate(context.getNextFireTime()))
          ;

        createOperationJobDetail(op, context.getJobDetail());
        if (collectExtraInformation()) {
            createOperationTrigger(op, context.getTrigger());
        }
        return op;
    }

    Operation createOperationJobDetail (Operation op, JobDetail detail) {
        String description =  detail.getDescription() == null ? "" : " - " + detail.getDescription();
        return op.label(detail.getFullName() + description)
          .putAnyNonEmpty("name", detail.getName())
          .putAnyNonEmpty("group", detail.getGroup())
          .putAnyNonEmpty("fullName", detail.getFullName())
          .putAnyNonEmpty("description", detail.getDescription())
          .put("jobClass", detail.getJobClass().getName())
          ;
    }

    OperationMap createOperationTrigger (Operation op, Trigger trigger) {
        return op.createMap("trigger")
                .put("priority", trigger.getPriority())
                .putAnyNonEmpty("name", trigger.getName())
                .putAnyNonEmpty("group", trigger.getGroup())
                .putAnyNonEmpty("fullName", trigger.getFullName())
                .putAnyNonEmpty("description", trigger.getDescription())
                .putAnyNonEmpty("jobName", trigger.getJobName())
                .putAnyNonEmpty("jobGroup", trigger.getJobGroup())
                .putAnyNonEmpty("fullJobName", trigger.getFullJobName())
                .putAnyNonEmpty("calendarName", trigger.getCalendarName())
                .putAnyNonEmpty("startTime", safeCloneDate(trigger.getStartTime()))
                .putAnyNonEmpty("endTime", safeCloneDate(trigger.getEndTime()))
                .putAnyNonEmpty("previousFireTime", safeCloneDate(trigger.getPreviousFireTime()))
                .putAnyNonEmpty("nextFireTime", safeCloneDate(trigger.getNextFireTime()))
                ;
    }

    /*
     *  We clone the dates in order to take a "snapshot" of them, in case
     *  they are actually re-used by their owner
     */
    Date safeCloneDate (Date value) {
        if (value == null) {
            return null;
        }

        return (Date) value.clone();
    }
    boolean collectExtraInformation ()
    {
        return FrameBuilder.OperationCollectionLevel.HIGH.equals(configuration.getCollectionLevel());
    }

    @Override
    public String getPluginName() {
        return "quartz-scheduler";
    }
}
