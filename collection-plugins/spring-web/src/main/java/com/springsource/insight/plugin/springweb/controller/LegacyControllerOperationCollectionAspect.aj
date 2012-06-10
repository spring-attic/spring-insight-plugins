/**
 * Copyright 2009-2011 the original author or authors.
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

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.SourceCodeLocation;
import com.springsource.insight.plugin.springweb.LegacyControllerPointcuts;
import com.springsource.insight.plugin.springweb.SpringWebPluginRuntimeDescriptor;

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
public aspect LegacyControllerOperationCollectionAspect
    extends MethodOperationCollectionAspect
{
    public pointcut collectionPoint() : LegacyControllerPointcuts.controllerHandlerMethod();

    @Override
    public Operation createOperation(JoinPoint jp) {
        return super.createOperation(jp).type(ControllerEndPointAnalyzer.CONTROLLER_METHOD_TYPE);
    }

    @Override
    public boolean isEndpoint() {
        return true;
    }

    @Override
    public SourceCodeLocation getSourceCodeLocation(JoinPoint jp) {
        MethodSignature mSig = (MethodSignature) jp.getSignature();
        String className = jp.getTarget().getClass().getName();
        return new SourceCodeLocation(className, mSig.getName(), 1);
    }

    @Override
    public String getPluginName() {
        return SpringWebPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
