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

package com.springsource.insight.plugin.springweb.view.resolver;

import java.util.Locale;

import org.aspectj.lang.JoinPoint;
import org.springframework.web.servlet.View;

import com.springsource.insight.collection.DefaultOperationCollector;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.plugin.springweb.AbstractSpringWebAspectSupport;
import com.springsource.insight.plugin.springweb.ControllerPointcuts;
import com.springsource.insight.plugin.springweb.view.ViewUtils;

public aspect ViewResolverOperationCollectionAspect extends AbstractSpringWebAspectSupport {
    private static final OperationType TYPE = OperationType.valueOf("view_resolver");
    
    public ViewResolverOperationCollectionAspect() {
        super(new ViewResolverMetricCollector());
    }

    public pointcut collectionPoint() : ControllerPointcuts.resolveView();

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[] args = jp.getArgs();
        String viewName = (String) args[0];
        Locale locale = (Locale) args[1];

        return new Operation()
            .label("Resolve view \"" + viewName + "\"")
            .type(TYPE)
            .sourceCodeLocation(getSourceCodeLocation(jp))
            .put("viewName", viewName)
            .put("locale", locale.toString())
            ;
    }

    static class ViewResolverMetricCollector extends DefaultOperationCollector {
    	ViewResolverMetricCollector () {
    		super();
    	}

        @Override
        public void processNormalExit(Operation op, Object returnValue) {
            if (returnValue instanceof View) {
                View view = (View) returnValue;
                op.put("resolvedView", ViewUtils.getViewDescription(view))
                    .put("contentType", view.getContentType());
            }
        }
    }
}
