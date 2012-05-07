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

package com.springsource.insight.plugin.springweb.request;

import static com.springsource.insight.intercept.operation.OperationFields.EXCEPTION;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.springsource.insight.plugin.springweb.AbstractSpringWebAspectSupport;
import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.collection.DefaultOperationCollector;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.plugin.springweb.SpringWebPointcuts;
import com.springsource.insight.util.StringUtil;

public aspect WebRequestOperationCollectionAspect
    extends AbstractSpringWebAspectSupport
{
    public pointcut collectionPoint() : SpringWebPointcuts.processWebRequest();
    
    public WebRequestOperationCollectionAspect() {
        super(new WebRequestOperationCollector());
    }

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[] args = jp.getArgs();

        HttpServletRequest request = (HttpServletRequest) args[0];
        HttpServletResponse response = (HttpServletResponse) args[1];

        return new Operation()
            .label("Spring Web Dispatch")
            .type(OperationType.WEB_REQUEST)
            .sourceCodeLocation(getSourceCodeLocation(jp))
            .put("method", request.getMethod())
            .put("uri", request.getRequestURI());
    }

    private static class WebRequestOperationCollector extends DefaultOperationCollector {

        @Override
        protected void processNormalExit(Operation op) {
            op.put("error", false);
        }

        @Override
        protected void processNormalExit(Operation op, Object returnValue) {
            op.put("error", false);
        }

        @Override
        protected void processAbnormalExit(Operation op, Throwable throwable) {
            op.put("error", true)
                .put(EXCEPTION, StringUtil.throwableToString(throwable));
            
        }
        
    }
    
}
