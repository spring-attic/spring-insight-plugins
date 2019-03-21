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

package com.springsource.insight.plugin.webflow;

import org.aspectj.lang.JoinPoint;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.AnnotatedAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;

/**
 * This aspect create insight operation for Webflow Action activity
 * @type: wf-action
 * @properties: action
 */
public privileged aspect ActionOperationCollectionAspect extends AbstractOperationCollectionAspect {
    public ActionOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint()
            : execution(Event org.springframework.webflow.execution.ActionExecutor.execute(Action, RequestContext));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[] args = jp.getArgs();
        String expression = OperationCollectionUtils.getActionExpression((AnnotatedAction) args[0]);

        return new Operation().type(OperationCollectionTypes.ACTION_TYPE.type)
                .label(OperationCollectionTypes.ACTION_TYPE.label + " [" + expression + "]")
                .sourceCodeLocation(getSourceCodeLocation(jp))
                .put("action", expression);
    }

    @Override
    public String getPluginName() {
        return WebflowPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
