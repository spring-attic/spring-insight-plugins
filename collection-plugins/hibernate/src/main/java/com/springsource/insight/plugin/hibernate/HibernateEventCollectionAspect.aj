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
package com.springsource.insight.plugin.hibernate;

import org.aspectj.lang.JoinPoint;
import org.hibernate.event.DirtyCheckEventListener;
import org.hibernate.event.FlushEventListener;
import org.hibernate.event.def.AbstractFlushingEventListener;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.operation.Operation;

public aspect HibernateEventCollectionAspect extends MethodOperationCollectionAspect {
	/**
	 * Default score assigned to the Hibernate events if they are considered
	 * to be endpoints. We use a score slightly above the ceiling so that it
	 * trumps the &quot;normal&quot; candidates (e.g., HTTP, queues, etc.),
	 * but not other Spring beans/services that may be invoked as a result
	 */
	public static final int	ENDPOINT_SCORE=EndPointAnalysis.CEILING_LAYER_SCORE + 1;

	public HibernateEventCollectionAspect () {
		super();
	}

    public pointcut dirtyCheck()
        : execution(void DirtyCheckEventListener.onDirtyCheck(..));

    public pointcut flushEvent()
        : execution(void FlushEventListener.onFlush(..));

    public pointcut abstractPrepareFlushing()
        : execution(* AbstractFlushingEventListener.prepare*(..));

    public pointcut abstractFlushing()
        : execution(* AbstractFlushingEventListener.flush*(..));

    public pointcut collectionPoint()
        : dirtyCheck() || flushEvent() || abstractPrepareFlushing() || abstractFlushing();

    @Override
    public Operation createOperation(JoinPoint jp) {
        return super.createOperation(jp)
                    .label("Hibernate " + jp.getStaticPart().getSignature().getName())
                    .put(EndPointAnalysis.SCORE_FIELD, ENDPOINT_SCORE)
                    ;
    }

    @Override
    public String getPluginName() {
        return HibernatePluginRuntimeDescriptor.PLUGIN_NAME;
    }

}
