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
package com.springsource.insight.plugin.jsf;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

public aspect PhaseListenerOperationCollectionAspect extends AbstractJSFOperationCollectionAspect {

	static final OperationType TYPE = OperationType.valueOf("jsf_phase_listener_operation");

	public pointcut collectionPoint()
        : (execution(public void PhaseListener.beforePhase(PhaseEvent))
            || execution(public void PhaseListener.afterPhase(PhaseEvent)))
            && !(within(com.sun.faces.**) || within(org.apache.myfaces.**));

	@Override
	protected Operation createOperation(JoinPoint jp) {
		PhaseEvent phaseEvent = (PhaseEvent) jp.getArgs()[0];
		PhaseId phaseId = phaseEvent.getPhaseId();

		StringBuilder label = new StringBuilder("JSF Phase Listener [");
		label.append(jp.getTarget().getClass().getSimpleName());
		label.append("#");
		label.append(jp.getSignature().getName());
		label.append("]");
		return super.createOperation(jp).type(TYPE).label(label.toString())
				.put("phaseId", phaseId.toString());
	}
}
