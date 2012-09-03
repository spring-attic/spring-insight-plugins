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
import java.util.logging.Logger;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.trace.FrameBuilder;
import com.springsource.insight.util.StringUtil;

/**
 * <B>Note:</B> the aspect uses reflective access in order to be compatible with
 * version 1.8 as well as 2.0 and 2.1
 */
public aspect QuartzSchedulerOperationCollectionAspect extends MethodOperationCollectionAspect {
    private static final InterceptConfiguration configuration = InterceptConfiguration.getInstance();
    private final Logger	logger=Logger.getLogger(getClass().getName());

    public QuartzSchedulerOperationCollectionAspect () {
    	super();
    }

    public pointcut collectionPoint()
        : execution(* org.quartz.Job+.execute(..))
        ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Operation   op=super.createOperation(jp).type(QuartzSchedulerDefinitions.TYPE);
        Object[]	args=jp.getArgs();
        return createOperation(op, args[0]);
    }

    Operation createOperation (Operation op, Object context) {
    	QuartzJobExecutionContextValueAccessor	contextAccessor=QuartzJobExecutionContextValueAccessor.getInstance();
    	try {
	        op.putAnyNonEmpty("fireTime", safeCloneDate(contextAccessor.getFireTime(context)))
	          .putAnyNonEmpty("refireCount", contextAccessor.getRefireCount(context))
	          .putAnyNonEmpty("scheduledFireTime", safeCloneDate(contextAccessor.getScheduledFireTime(context)))
	          .putAnyNonEmpty("previousFireTime", safeCloneDate(contextAccessor.getPreviousFireTime(context)))
	          .putAnyNonEmpty("nextFireTime", safeCloneDate(contextAccessor.getNextFireTime(context)))
	          ;
    	} catch(Error e) {
    		logger.warning("Failed (" + e.getClass().getSimpleName() + ")"
    				     + " to populate context values: " + e.getMessage());
    	}

        Object	jobDetail=contextAccessor.getJobDetail(context);
        try {
        	createOperationJobDetail(op, jobDetail);
    	} catch(Error e) {
    		logger.warning("Failed (" + e.getClass().getSimpleName() + ")"
    				     + " to populate job details values: " + e.getMessage());
    	}

        if (collectExtraInformation()) {
            try {
            	createOperationTrigger(op, contextAccessor.getTrigger(context), jobDetail);
        	} catch(Error e) {
        		logger.warning("Failed (" + e.getClass().getSimpleName() + ")"
        				     + " to populate trigger values: " + e.getMessage());
        	}
        }

        return op;
    }

    Operation createOperationJobDetail (Operation op, Object detail) {
    	QuartzJobDetailValueAccessor	detailAccessor=QuartzJobDetailValueAccessor.getInstance();
        String description=detailAccessor.getDescription(detail);
        Object jobKey=detailAccessor.getKey(detail);
        Class<?> jobClass=detailAccessor.getJobClass(detail);

        QuartzKeyValueAccessor	keyAccessor=QuartzKeyValueAccessor.getInstance();
   		String jobGroup=keyAccessor.getGroup(jobKey);
   		String jobName=keyAccessor.getName(jobKey);
        String fullName=keyAccessor.getFullName(jobKey);

        return op.label(fullName + (StringUtil.isEmpty(description) ? "" : " - " + description))
        		 .putAnyNonEmpty("name", jobName)
        		 .putAnyNonEmpty("group", jobGroup)
        		 .putAnyNonEmpty("fullName", fullName)
        		 .putAnyNonEmpty("description", description)
        		 .putAnyNonEmpty("jobClass", (jobClass == null) ? null : jobClass.getName())
        		 ;
    }

    OperationMap createOperationTrigger (Operation op, Object trigger, Object jobDetail) {
    	QuartzTriggerValueAccessor	triggerAccessor=QuartzTriggerValueAccessor.getInstance();
    	Object	triggerKey=triggerAccessor.getKey(trigger);

        QuartzKeyValueAccessor	keyAccessor=QuartzKeyValueAccessor.getInstance();
   		String 	triggerGroup=keyAccessor.getGroup(triggerKey);
   		String 	triggerName=keyAccessor.getName(triggerKey);
   		String	triggerFullName=keyAccessor.getFullName(triggerKey);

    	QuartzJobDetailValueAccessor	detailAccessor=QuartzJobDetailValueAccessor.getInstance();
   		Object	jobKey=detailAccessor.getKey(jobDetail);
   		String 	jobGroup=keyAccessor.getGroup(jobKey);
   		String 	jobName=keyAccessor.getName(jobKey);
   		String	jobFullName=keyAccessor.getFullName(jobKey);
    	
   		return op.createMap("trigger")
                 .putAnyNonEmpty("priority", triggerAccessor.getPriority(trigger))
                 .putAnyNonEmpty("description", triggerAccessor.getDescription(trigger))
   
                 .putAnyNonEmpty("name", triggerName)
                 .putAnyNonEmpty("group", triggerGroup)
                 .putAnyNonEmpty("fullName", triggerFullName)
                
                 .putAnyNonEmpty("jobName", jobName)
                 .putAnyNonEmpty("jobGroup", jobGroup)
                 .putAnyNonEmpty("fullJobName", jobFullName)
                
                 .putAnyNonEmpty("calendarName", triggerAccessor.getCalendarName(trigger))
                 .putAnyNonEmpty("startTime", safeCloneDate(triggerAccessor.getStartTime(trigger)))
                 .putAnyNonEmpty("endTime", safeCloneDate(triggerAccessor.getEndTime(trigger)))
                 .putAnyNonEmpty("previousFireTime", safeCloneDate(triggerAccessor.getPreviousFireTime(trigger)))
                 .putAnyNonEmpty("nextFireTime", safeCloneDate(triggerAccessor.getNextFireTime(trigger)))
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
