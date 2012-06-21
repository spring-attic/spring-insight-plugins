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

package com.springsource.insight.plugin.springweb.binder;

import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.springframework.validation.DataBinder;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFinalizer;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.plugin.springweb.AbstractSpringWebAspectSupport;
import com.springsource.insight.plugin.springweb.ControllerPointcuts;

public aspect InitBinderOperationCollectionAspect extends AbstractSpringWebAspectSupport {
    static final OperationType TYPE = OperationType.valueOf("init_binder");

    public InitBinderOperationCollectionAspect () {
    	super();
    }
    
    public pointcut collectionPoint() : ControllerPointcuts.initBinder();

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Operation operation = new Operation()
            .type(TYPE)
            .sourceCodeLocation(getSourceCodeLocation(jp));
        InitBinderOperationFinalizer.register(operation, jp);
        return operation;
    }

    private static class InitBinderOperationFinalizer implements OperationFinalizer {
        private static final InitBinderOperationFinalizer INSTANCE = new InitBinderOperationFinalizer();
        private static final String JOINPOINT_KEY = InitBinderOperationFinalizer.class.getName() + "#JOINPOINT_KEY";

        InitBinderOperationFinalizer () {
        	super();
        }

        public static void register(Operation operation, JoinPoint jp) {
            operation.addFinalizer(INSTANCE)
                .addFinalizerObject(JOINPOINT_KEY, jp);
        }
        
        public void finalize(Operation operation, Map<String, Object> richObjects) {
            JoinPoint jp = (JoinPoint) richObjects.get(JOINPOINT_KEY);
            DataBinder binder = extractDataBinderArg(jp);
            if (binder != null) {
                String objectName = binder.getObjectName();
                operation.label("Init Binder " + objectName)
                    .put("objectName", objectName);
                Object target = binder.getTarget();
                if (target == null) {
                    // Target object may be null according to WebDataBinder docs
                    operation.put("targetType", "unknown");
                } else {
                    operation.put("targetType", target.getClass().getName());
                }
                fromArray(operation.createList("allowedFields"), binder.getAllowedFields());
                fromArray(operation.createList("disallowedFields"), binder.getDisallowedFields());
                fromArray(operation.createList("requiredFields"), binder.getRequiredFields());
            }
        }
        
        private void fromArray(OperationList operationList, String[] array) {
            if (array == null) {
                return;
            }
            for (String item : array) {
                operationList.add(item);
            }
        }
        
        private DataBinder extractDataBinderArg(JoinPoint jp) {
            Object[] args = jp.getArgs();
            for (Object arg : args) {
                if (arg instanceof DataBinder) {
                    return (DataBinder)arg;
                }
            }
            return null;
        }
        
    }
    
}
