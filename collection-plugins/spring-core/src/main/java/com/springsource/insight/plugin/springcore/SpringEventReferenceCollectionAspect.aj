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

package com.springsource.insight.plugin.springcore;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

import com.springsource.insight.intercept.operation.OperationType;

/**
 * 
 */
public abstract aspect SpringEventReferenceCollectionAspect extends SpringLifecycleMethodOperationCollectionAspect {
	protected SpringEventReferenceCollectionAspect (OperationType opType) {
		super(opType);
	}

	@Override
	protected String resolveEventData (Object event) {
		if (event == null) {
			return Object.class.getName();
		}

		Class<?>	eventType=event.getClass();
		if (ApplicationContextEvent.class.isAssignableFrom(eventType)) {
			ApplicationContext	context=((ApplicationContextEvent) event).getApplicationContext();
			return eventType.getSimpleName() + ": " + context.getDisplayName();
		} else {
			return eventType.getName();
		}
	}
}
