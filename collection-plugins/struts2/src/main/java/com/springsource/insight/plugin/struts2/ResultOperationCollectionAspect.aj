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

package com.springsource.insight.plugin.struts2;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aspectj.lang.JoinPoint;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ValidationAware;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;

/**
 * Collection operation for Struts2 overall action result  
 */
public privileged aspect ResultOperationCollectionAspect extends AbstractOperationCollectionAspect {
    public ResultOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint():
            execution(void org.apache.struts2.dispatcher.ServletDispatcherResult.doExecute(String, ActionInvocation));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[] args = jp.getArgs();
        ActionInvocation aci = (ActionInvocation) args[1];

        // get resolve view
        String view = (String) args[0];
        // get result code
        String result = aci.getResultCode();

        // get fields validation errors
        Map<String, List<String>> errs = null;
        Object action = aci.getAction();
        if (action instanceof ValidationAware) {
            errs = ((ValidationAware) action).getFieldErrors();
        }

        Operation operation = new Operation().type(OperationCollectionTypes.RESULT_TYPE.type)
                .label(OperationCollectionTypes.RESULT_TYPE.label + " [" + result + "]")
                .sourceCodeLocation(getSourceCodeLocation(jp))
                .put("view", view)
                .put("resultCode", result);

        if (errs != null && !errs.isEmpty()) {
            // add fields validation errors
            OperationMap map = operation.createMap("errs");
            Set<Entry<String, List<String>>> entries = errs.entrySet();
            for (Entry<String, List<String>> item : entries) {
                map.put(item.getKey(), item.getValue().get(0));
            }
        }

        return operation;
    }

    @Override
    public String getPluginName() {
        return Struts2PluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
