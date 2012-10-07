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

package com.springsource.insight.plugin.springweb.binder;

import org.aspectj.lang.JoinPoint;
import org.springframework.validation.DataBinder;

import com.springsource.insight.collection.method.JoinPointFinalizer;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.util.ArrayUtil;

/**
 * 
 */
class InitBinderOperationFinalizer extends JoinPointFinalizer {
    private static final InitBinderOperationFinalizer INSTANCE = new InitBinderOperationFinalizer();

    public static final String	TARGET_TYPE="targetType", UNKNOWN_TARGET_TYPE="unknown";
    public static final String	OBJECT_NAME="objectName";
    /**
     * Name of the {@link OperationList} containing the allowed fields names
     */
    public static final String	ALLOWED_FIELDS_LIST="allowedFields";
    /**
     * Name of the {@link OperationList} containing the disallowed fields names
     */
    public static final String	DISALLOWED_FIELDS_LIST="disallowedFields";
    /**
     * Name of the {@link OperationList} containing the required fields names
     */
    public static final String	REQUIRED_FIELDS_LIST="requiredFields";

    private InitBinderOperationFinalizer () {
    	super();
    }

    public static final InitBinderOperationFinalizer getInstance () {
    	return INSTANCE;
    }

    public void registerBinderOperation (Operation op, JoinPoint jp) {
    	registerWithSelf(op, jp);
    }

	@Override
	protected void populateOperation(Operation op, JoinPoint jp) {
		super.populateOperation(op, jp);

        DataBinder binder = extractDataBinderArg(jp);
        if (binder == null) {
        	return;
        }

        String objectName = binder.getObjectName();
        op.label("Init Binder " + objectName)
           .put(OBJECT_NAME, objectName);

        Object target = binder.getTarget();
        if (target == null) {
        	// Target object may be null according to WebDataBinder docs
        	op.put(TARGET_TYPE, UNKNOWN_TARGET_TYPE);
        } else {
        	op.put(TARGET_TYPE, target.getClass().getName());
        }

        fromArray(op.createList(ALLOWED_FIELDS_LIST), binder.getAllowedFields());
        fromArray(op.createList(DISALLOWED_FIELDS_LIST), binder.getDisallowedFields());
        fromArray(op.createList(REQUIRED_FIELDS_LIST), binder.getRequiredFields());
    }
    
    private static OperationList fromArray(OperationList operationList, String ... array) {
        if (ArrayUtil.length(array) <= 0) {
            return operationList;
        }

        for (String item : array) {
            operationList.add(item);
        }

        return operationList;
    }
    
    private static DataBinder extractDataBinderArg(JoinPoint jp) {
    	return extractDataBinderArg(jp.getArgs());
    }

    private static DataBinder extractDataBinderArg(Object ... args) {
        if (ArrayUtil.length(args) <= 0) {
        	return null;
        }

        for (Object arg : args) {
            if (arg instanceof DataBinder) {
                return (DataBinder)arg;
            }
        }
        return null;
    }
}
