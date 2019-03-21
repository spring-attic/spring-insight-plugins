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

package com.springsource.insight.plugin.springweb.modelattribute;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Conventions;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.springsource.insight.collection.DefaultOperationCollector;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.util.StringFormatterUtils;
import com.springsource.insight.util.StringUtil;

/**
 *
 */
class ModelAttributeOperationCollector extends DefaultOperationCollector {
    /**
     * The {@link String} operation attribute holding the model attribute name
     */
    public static final String MODEL_ATTR_NAME = "modelAttributeName";
    /**
     * The formatted {@link String} attribute value
     */
    public static final String MODEL_ATTR_VALUE = "modelAttributeValue";

    public static final OperationType TYPE = OperationType.valueOf("model_attribute");

    ModelAttributeOperationCollector() {
        super();
    }

    @Override
    protected void processNormalExit(Operation op, Object returnValue) {
        String attrValue = StringFormatterUtils.formatObject(returnValue);
        op.put(MODEL_ATTR_VALUE, attrValue);
    }

    static String extractModelAttributeName(JoinPoint jp) {
        Signature sig = jp.getSignature();
        Method method = (sig instanceof MethodSignature) ? ((MethodSignature) sig).getMethod() : null;
        ModelAttribute ma = (method == null) ? null : method.getAnnotation(ModelAttribute.class);
        String modelAttrName = (ma == null) ? null : ma.value();
        if (!StringUtil.isEmpty(modelAttrName)) {
            return modelAttrName;
        }

        if (method == null) {
            return ModelAttribute.class.getSimpleName();
        } else {
            return Conventions.getVariableNameForReturnType(method);
        }
    }
}
