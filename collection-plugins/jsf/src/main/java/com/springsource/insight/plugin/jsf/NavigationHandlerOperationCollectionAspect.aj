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

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

public aspect NavigationHandlerOperationCollectionAspect extends AbstractJSFOperationCollectionAspect {

	static final OperationType TYPE = OperationType.valueOf("jsf_navigation_operation");

	public pointcut collectionPoint()
        : execution(public void NavigationHandler.handleNavigation(FacesContext, String, String));

	@Override
	protected Operation createOperation(JoinPoint jp) {
        String fromAction = jp.getArgs()[1] != null ? jp.getArgs()[1].toString() : "No fromAction";
        String outcome = jp.getArgs()[2] != null ? jp.getArgs()[2].toString() : "No outcome";

		StringBuilder label = new StringBuilder("JSF Navigation [");
		label.append(outcome);
		label.append("]");
		return super.createOperation(jp).type(TYPE).label(label.toString())
				.put("fromAction", fromAction)
				.put("outcome", outcome);
	}
}
