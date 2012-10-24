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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ApplicationEventMulticaster;

import com.springsource.insight.collection.method.AnnotationDrivenMethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public aspect EventPublisingOperationCollectionAspect extends SpringLifecycleMethodOperationCollectionAspect {
	public static final String	ACTION_ATTR="actionType", EVENT_ATTR="eventType", ACTION_SUFFIX="Event";

	public EventPublisingOperationCollectionAspect () {
		super();
	}

	public pointcut publishingPoint ()
		: execution(* ApplicationEventPublisher+.publishEvent(ApplicationEvent))
	   || execution(* ApplicationEventMulticaster+.multicastEvent(ApplicationEvent))
	    ;

	public pointcut collectionPoint()
		: publishingPoint()
	  && !AnnotationDrivenMethodOperationCollectionAspect.collectionPoint()
	    ;

	@Override
	protected Operation createOperation(JoinPoint jp) {
		Signature	sig=jp.getSignature();
		String		name=sig.getName();
		if (name.endsWith(ACTION_SUFFIX) && (name.length() > ACTION_SUFFIX.length())) {
			name = name.substring(0, name.length() - ACTION_SUFFIX.length());
		}

		Object	event=jp.getArgs()[0];
		return super.createOperation(jp)
					.put(ACTION_ATTR, name)
					.put(EVENT_ATTR, (event == null) ? Object.class.getName() : event.getClass().getName())
					;
	}
}
