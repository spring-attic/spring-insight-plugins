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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.plugin.springweb.AbstractSpringWebAspectSupport;

public aspect ModelAttributeOperationCollectionAspect extends AbstractSpringWebAspectSupport {
    public ModelAttributeOperationCollectionAspect() {
        super(new ModelAttributeOperationCollector());
    }

    public pointcut collectionPoint(): execution(@ModelAttribute !@RequestMapping !void *(..));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        MethodSignature sig = (MethodSignature) jp.getSignature();
        String methodString = sig.getDeclaringType().getSimpleName() + "#" + sig.getName();
        return new Operation()
                .label("@ModelAttribute " + methodString)
                .type(ModelAttributeOperationCollector.TYPE)
                .sourceCodeLocation(getSourceCodeLocation(jp))
                .put(ModelAttributeOperationCollector.MODEL_ATTR_NAME, ModelAttributeOperationCollector.extractModelAttributeName(jp))
                ;
    }
}
