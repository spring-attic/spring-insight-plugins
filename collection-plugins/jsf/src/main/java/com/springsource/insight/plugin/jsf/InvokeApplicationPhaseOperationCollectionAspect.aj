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

import javax.faces.context.FacesContext;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

public aspect InvokeApplicationPhaseOperationCollectionAspect extends AbstractJSFOperationCollectionAspect {

	public static final OperationType TYPE = OperationType
			.valueOf("invoke_application_phase_operation");

	public pointcut collectionPoint()
        : execution(boolean org.apache.myfaces.lifecycle.InvokeApplicationExecutor.execute(FacesContext))
        	|| execution (void com.sun.faces.lifecycle.InvokeApplicationPhase.execute(FacesContext));

	@Override
	protected Operation createOperation(JoinPoint jp) {
		return super.createOperation(jp).type(TYPE)
				.label("JSF Invoke Apllication Phase");
	}
}
