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

package com.springsource.insight.plugin.springweb.view.render;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.JoinPoint;
import org.springframework.web.servlet.View;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.plugin.springweb.AbstractSpringWebAspectSupport;
import com.springsource.insight.plugin.springweb.view.ViewUtils;
import com.springsource.insight.util.StringFormatterUtils;

public aspect ViewRenderOperationCollectionAspect extends AbstractSpringWebAspectSupport {
    static final OperationType TYPE = OperationType.valueOf("view_render");

    public ViewRenderOperationCollectionAspect() {
        super();
    }

    // Specifying generic parameters causes a mismatch for Spring 2.5 applications
    public pointcut collectionPoint()
            : execution(void View.render(Map/*<String, ?>*/, HttpServletRequest, HttpServletResponse));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        View view = (View) jp.getThis();
        String resolvedView = ViewUtils.getViewDescription(view);
        String viewType = view.getClass().getName();
        String contentType = view.getContentType();
        Operation operation = new Operation()
                .label("Render view " + resolvedView)
                .type(TYPE)
                .sourceCodeLocation(getSourceCodeLocation(jp))
                .put("resolvedView", resolvedView)
                .put("viewType", viewType)
                .put("contentType", contentType);
        // TODO move into a finalizer
        OperationMap model = operation.createMap("model");
        for (Map.Entry<String, String> entry : StringFormatterUtils.formatMap((Map<?, ?>) jp.getArgs()[0]).entrySet()) {
            model.put(entry.getKey(), entry.getValue());
        }
        return operation;
    }

}
