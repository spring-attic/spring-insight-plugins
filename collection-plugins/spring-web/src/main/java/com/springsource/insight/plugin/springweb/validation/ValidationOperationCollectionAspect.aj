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

package com.springsource.insight.plugin.springweb.validation;

import org.aspectj.lang.JoinPoint;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.plugin.springweb.AbstractSpringWebAspectSupport;

public aspect ValidationOperationCollectionAspect extends AbstractSpringWebAspectSupport {
	public ValidationOperationCollectionAspect () {
		super();
	}

    public pointcut collectionPoint() : execution(* Validator+.validate(Object, Errors));

    @Override
	protected Operation createOperation(JoinPoint jp) {
        Operation op = new Operation()
        				.type(ValidationErrorsMetricsGenerator.TYPE)
        				.sourceCodeLocation(getSourceCodeLocation(jp))
        				.put(EndPointAnalysis.SCORE_FIELD, EndPointAnalysis.CEILING_LAYER_SCORE)
        				;
        ValidationJoinPointFinalizer	finalizer=ValidationJoinPointFinalizer.getInstance();
        finalizer.registerValidationOperation(op, jp);
        return op;
	}
}
