/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.insight.plugin.struts2;

import org.aspectj.lang.JoinPoint;

import com.opensymphony.xwork2.ActionInvocation;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;

/**
 * Collection operation for Struts2 custom interceptors,
 * Do not collect internal Struts2 interceptors.
 */
public privileged aspect InterceptOperationCollectionAspect extends AbstractOperationCollectionAspect {
    public InterceptOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint():
            execution(String com.opensymphony.xwork2.interceptor.Interceptor+.intercept(ActionInvocation))
                    // do not collect internal Struts2 interceptors
                    && !within(com.opensymphony.xwork2.interceptor..*) && !within(org.apache.struts2.interceptor..*);

    @Override
    protected Operation createOperation(JoinPoint jp) {
        String interceptor = jp.getThis().getClass().getName();

        return new Operation().type(OperationCollectionTypes.INTERCEPT_TYPE.type)
                .label(OperationCollectionTypes.INTERCEPT_TYPE.label + " [" + interceptor + "]")
                .sourceCodeLocation(getSourceCodeLocation(jp))
                .put("interceptor", interceptor);  // interceptor signature: ClassName
    }

    @Override
    public String getPluginName() {
        return Struts2PluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
