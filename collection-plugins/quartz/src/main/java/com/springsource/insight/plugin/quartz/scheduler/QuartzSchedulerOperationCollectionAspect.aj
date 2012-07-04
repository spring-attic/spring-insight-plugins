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
import java.util.logging.Logger;

import org.aspectj.lang.JoinPoint;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.trace.FrameBuilder;
import com.springsource.insight.util.StringUtil;

/**
 * 
 */
public aspect QuartzSchedulerOperationCollectionAspect extends MethodOperationCollectionAspect {
    private static final InterceptConfiguration configuration = InterceptConfiguration.getInstance();
    private final Logger	logger=Logger.getLogger(getClass().getName());
    private final QuartzKeyValueAccessor	keyAccessor=QuartzKeyValueAccessor.getInstance();

    public QuartzSchedulerOperationCollectionAspect () {
    	super();
    }

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
    	try {
	        op.putAnyNonEmpty("fireTime", safeCloneDate(context.getFireTime()))
	          .put("refireCount", context.getRefireCount())
	          .putAnyNonEmpty("scheduledFireTime", safeCloneDate(context.getScheduledFireTime()))
	          .putAnyNonEmpty("previousFireTime", safeCloneDate(context.getPreviousFireTime()))
	          .putAnyNonEmpty("nextFireTime", safeCloneDate(context.getNextFireTime()))
	          ;
    	} catch(Error e) {
    		logger.warning("Failed (" + e.getClass().getSimpleName() + ")"
    				     + " to populate context values: " + e.getMessage());
    	}

        JobDetail	jobDetail=context.getJobDetail();
        try {
        	createOperationJobDetail(op, jobDetail);
    	} catch(Error e) {
    		logger.warning("Failed (" + e.getClass().getSimpleName() + ")"
    				     + " to populate job details values: " + e.getMessage());
    	}

        if (collectExtraInformation()) {
            try {
            	createOperationTrigger(op, context.getTrigger(), jobDetail);
        	} catch(Error e) {
        		logger.warning("Failed (" + e.getClass().getSimpleName() + ")"
        				     + " to populate trigger values: " + e.getMessage());
        	}
        }

        return op;
    }

    Operation createOperationJobDetail (Operation op, JobDetail detail) {
        String description=detail.getDescription();
        Object jobKey=detail.getKey();
   		String jobGroup=keyAccessor.getGroup(jobKey);
   		String jobName=keyAccessor.getName(jobKey);
        String fullName=keyAccessor.getFullName(jobKey);
        
        return op.label(fullName + (StringUtil.isEmpty(description) ? "" : " - " + description))
        		 .putAnyNonEmpty("name", jobName)
        		 .putAnyNonEmpty("group", jobGroup)
        		 .putAnyNonEmpty("fullName", fullName)
        		 .putAnyNonEmpty("description", description)
        		 .put("jobClass", detail.getJobClass().getName())
        		 ;
    }

    OperationMap createOperationTrigger (Operation op, Trigger trigger, JobDetail jobDetail) {
    	Object	triggerKey=trigger.getKey();
   		String 	triggerGroup=keyAccessor.getGroup(triggerKey);
   		String 	triggerName=keyAccessor.getName(triggerKey);
   		String	triggerFullName=keyAccessor.getFullName(triggerKey);

   		Object	jobKey=jobDetail.getKey();
   		String 	jobGroup=keyAccessor.getGroup(jobKey);
   		String 	jobName=keyAccessor.getName(jobKey);
   		String	jobFullName=keyAccessor.getFullName(jobKey);
    	
   		return op.createMap("trigger")
                 .put("priority", trigger.getPriority())
                 .putAnyNonEmpty("description", trigger.getDescription())
   
                 .putAnyNonEmpty("name", triggerName)
                 .putAnyNonEmpty("group", triggerGroup)
                 .putAnyNonEmpty("fullName", triggerFullName)
                
                 .putAnyNonEmpty("jobName", jobName)
                 .putAnyNonEmpty("jobGroup", jobGroup)
                 .putAnyNonEmpty("fullJobName", jobFullName)
                
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
    static Date safeCloneDate (Date value) {
        if (value == null) {
            return null;
        } else {
        	return (Date) value.clone();
        }
    }

    boolean collectExtraInformation () {
        return FrameBuilder.OperationCollectionLevel.HIGH.equals(configuration.getCollectionLevel());
    }

    @Override
    public String getPluginName() {
        return QuartzPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
