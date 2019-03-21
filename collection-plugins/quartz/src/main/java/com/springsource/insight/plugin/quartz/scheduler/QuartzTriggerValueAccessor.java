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

import java.util.Date;

/**
 * 
 */
public class QuartzTriggerValueAccessor extends AbstractQuartzValueAccessor {
	private static class LazyFieldHolder {
		static final QuartzTriggerValueAccessor	accessor=new QuartzTriggerValueAccessor();
	}

	public static QuartzTriggerValueAccessor getInstance () {
		return LazyFieldHolder.accessor;
	}

	QuartzTriggerValueAccessor () {
		super("org.quartz.Trigger");
	}
	
	public Object getKey (Object trigger) {
		return getProperty(trigger, "key", Object.class);
	}

	public String getDescription (Object trigger) {
		return getProperty(trigger, "description", String.class);
	}

	public String getCalendarName  (Object trigger) {
		return getProperty(trigger, "calendarName", String.class);
	}

	public Number getPriority (Object trigger) {
		return getProperty(trigger, "priority", Number.class);
	}

	public Date getStartTime (Object trigger) {
		return getProperty(trigger, "startTime", Date.class);
	}

	public Date getEndTime (Object trigger) {
		return getProperty(trigger, "endTime", Date.class);
	}

	public Date getPreviousFireTime (Object trigger) {
		return getProperty(trigger, "previousFireTime", Date.class);
	}

	public Date getNextFireTime (Object trigger) {
		return getProperty(trigger, "nextFireTime", Date.class);
	}
}
