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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

public aspect ValidatorOperationCollectionAspect extends AbstractJSFOperationCollectionAspect {

	static final OperationType TYPE = OperationType.valueOf("jsf_validator_operation");

	public pointcut collectionPoint()
        : execution(public void Validator.validate(FacesContext, UIComponent, Object));

	@Override
	protected Operation createOperation(JoinPoint jp) {
		UIComponent uiComponent = (UIComponent) jp.getArgs()[1];
		Object object = jp.getArgs()[2];

		StringBuilder label = new StringBuilder("JSF Validator [");
		label.append(jp.getTarget().getClass().getSimpleName());
		label.append("]");
		return super.createOperation(jp).type(TYPE).label(label.toString())
				.put("uiComponentId", uiComponent.getId())
				.put("value", object.toString());
	}

}
