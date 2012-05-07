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

package com.springsource.insight.plugin.grails;

import org.aspectj.lang.JoinPoint;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

public aspect GrailsControllerOperationCollectionAspect
    extends AbstractOperationCollectionAspect
{
    
    private static final OperationType TYPE = OperationType.valueOf("grails_controller_method");
    
    public pointcut collectionPoint() : GrailsControllerPointcuts.handleURIMethod();

    public GrailsControllerOperationCollectionAspect() {
        super(new GrailsControllerMetricCollector());
    }

    @Override
    public Operation createOperation(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String requestUri = (String)args[0];
        GrailsWebRequest webRequest = (GrailsWebRequest)args[1];        
        GrailsControllerStateKeeper.setThreadLocalWebRequest(webRequest);
        return new Operation()
            .type(TYPE)
            .put("requestUri", requestUri)
            .put("requestMethod", webRequest.getRequest().getMethod());
    }

    @Override
    public boolean isEndpoint() {
        return true; // This provides an EndPoint
    }

    @Override
    public String getPluginName() {
        return "grails";
    }
}
