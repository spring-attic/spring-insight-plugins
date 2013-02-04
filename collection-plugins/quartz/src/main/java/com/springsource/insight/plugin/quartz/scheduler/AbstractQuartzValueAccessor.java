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

import com.springsource.insight.util.logging.InsightLogManager;
import com.springsource.insight.util.logging.InsightLogger;
import com.springsource.insight.util.props.BeanPropertiesSource;

/**
 * 
 */
public abstract class AbstractQuartzValueAccessor extends BeanPropertiesSource {
	protected final InsightLogger	logger=InsightLogManager.getLogger(getClass().getName());
	protected AbstractQuartzValueAccessor (String beanClass) {
		super(beanClass, QuartzSchedulerDefinitions.class, true);
	}

	@Override
	public <T> T getProperty (Object target, String name, Class<T> attrType) {
		if (target == null) {
			return null;
		}

		try {
			return super.getProperty(target, name, attrType);
		} catch(Exception e) {
			logger.warning("getProperty(" + getBeanClass().getSimpleName() + "@" + name + ")"
						+ "[" + attrType.getSimpleName() + "]"
						+ " failed (" + e.getClass().getSimpleName() + ")"
						+ " to retrieve: " + e.getMessage());
			return null;
		}
	}
}
