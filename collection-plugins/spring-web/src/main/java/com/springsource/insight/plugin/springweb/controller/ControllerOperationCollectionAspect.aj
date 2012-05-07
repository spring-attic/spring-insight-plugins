/**
 * Copyright 2009-2010 the original author or authors.
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

package com.springsource.insight.plugin.springweb.controller;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.plugin.springweb.ControllerPointcuts;
import org.aspectj.lang.JoinPoint;

public aspect ControllerOperationCollectionAspect extends MethodOperationCollectionAspect {
    
    private static final OperationType TYPE = ControllerEndPointAnalyzer.CONTROLLER_METHOD_TYPE;
    
	public pointcut collectionPoint() : ControllerPointcuts.controllerHandlerMethod();

	@Override
    public Operation createOperation(JoinPoint jp) {
	    return super.createOperation(jp).type(TYPE);
    }

    @Override
    public boolean isEndpoint() {
        return true;
    }

    @Override
    public String getPluginName() {
        return "spring-web";
    }

}
