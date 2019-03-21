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

/**
 * 
 */
public class QuartzJobDetailValueAccessor extends AbstractQuartzValueAccessor {
	private static class LazyFieldHolder {
		static final QuartzJobDetailValueAccessor	accessor=new QuartzJobDetailValueAccessor();
	}

	public static QuartzJobDetailValueAccessor getInstance () {
		return LazyFieldHolder.accessor;
	}

	QuartzJobDetailValueAccessor () {
		super("org.quartz.JobDetail");
	}

	public String getDescription (Object detail) {
		return getProperty(detail, "description", String.class);
	}
	
	public Object getKey (Object detail) {
		return getProperty(detail, "key", Object.class);
	}
	
	public Class<?> getJobClass (Object detail) {
		return getProperty(detail, "jobClass", Class.class);
	}
}
