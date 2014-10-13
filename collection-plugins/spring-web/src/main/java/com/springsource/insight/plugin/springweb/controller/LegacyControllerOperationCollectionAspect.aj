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

package com.springsource.insight.plugin.springweb.controller;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;
import org.springframework.web.servlet.mvc.Controller;

import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.SourceCodeLocation;

/**
 * Creates an operation for implementors of the Spring MVC Controller
 * interface (pre annotated MVC).
 *
 * This class does something slightly different than the other controller
 * methods. Since there are several base classes which implement Controller, and
 * we do not want to handle them all, we simply identify the class as the target 
 * class that is being invoked (not the class which contains the handleRequest method).
 *
 * This means that the source code location may not actually point at the real file
 * which houses handleRequest, but should point at the user's code, which will have the
 * real handler implementation (albeit with a likely different name)
 */
public aspect LegacyControllerOperationCollectionAspect extends AbstractControllerOperationCollectionAspect {
    public LegacyControllerOperationCollectionAspect() {
        super(true);
    }

    public pointcut collectionPoint(): execution(* Controller+.handleRequest(..));

    @Override
    public Operation createOperation(JoinPoint jp) {
        return super.createOperation(jp)
                .put(EndPointAnalysis.SCORE_FIELD, ControllerEndPointAnalyzer.LEGACY_SCORE)
                ;
    }

    @Override
    public SourceCodeLocation getSourceCodeLocation(JoinPoint jp) {
        MethodSignature mSig = (MethodSignature) jp.getSignature();
        Object target = jp.getTarget();
        Class<?> targetClass = target.getClass();
        SourceLocation jpSource = jp.getSourceLocation();
        return new SourceCodeLocation(targetClass.getName(), mSig.getName(), jpSource.getLine());
    }
}
