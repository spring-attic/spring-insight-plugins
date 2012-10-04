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

package com.springsource.insight.plugin.springweb.validation;

import java.util.Collection;
import java.util.Collections;

import org.aspectj.lang.JoinPoint;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import com.springsource.insight.collection.method.JoinPointFinalizer;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationUtils;
import com.springsource.insight.intercept.trace.FrameBuilder;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;

/**
 * Used to traverse the validation {@link Errors}
 */
public class ValidationJoinPointFinalizer extends JoinPointFinalizer {
    private static final InterceptConfiguration configuration=InterceptConfiguration.getInstance();
	private static final ValidationJoinPointFinalizer	INSTANCE=new ValidationJoinPointFinalizer();
	/**
	 * A {@link Number} value indicating the number of reported errors
	 */
	public static final String	ERRORS_COUNT="validationErrorsCount";
	/**
	 * Name of an <U>optional</U> {@link OperationList} that contains
	 * the validation errors. Each error is encoded as an {@link OperationMap}
	 * with a &quot;name&quot; entry that contains the object name and
	 * a &quot;value&quot; entry that contains the error message.
	 * <B>Note:</B> the list is created only if then number of errors is
	 * <U>positive</U> and specific errors collection is enabled
	 * @see #ERRORS_COUNT
	 * @see #collectExtraInformation()
	 * @see OperationUtils#addNameValuePair(OperationList, String, String)
	 */
	public static final String	ERRORS_LIST="validationErrorsList";
	/**
	 * Maximum stored error text length 
	 */
	public static final int	MAX_ERROR_TEXT_LENGTH=96;

	private ValidationJoinPointFinalizer() {
		super();
	}
	
	public static final ValidationJoinPointFinalizer getInstance () {
		return INSTANCE;
	}

    public void registerValidationOperation (Operation op, JoinPoint jp) {
    	registerWithSelf(op, jp);
    }

	@Override
	protected void populateOperation(Operation op, JoinPoint jp) {
		super.populateOperation(op, jp);

		Object[]	args=jp.getArgs();
		populateOperation(op, (Errors) args[1], collectExtraInformation());
	}

	static final Operation populateOperation (Operation op, Errors errors, boolean collectErrorsList) {
		int	numErrors=(errors == null) ? 0 : errors.getErrorCount();
		op.put(ERRORS_COUNT, numErrors);

		if ((numErrors > 0) && collectErrorsList) {
			populateOperation(op.createList(ERRORS_LIST), errors);
		}

		return op;
	}
	
	static final OperationList populateOperation (OperationList errList, Errors errors) {
		Collection<? extends ObjectError>	errDetails=(errors == null)
					? Collections.<ObjectError>emptyList()
					: errors.getAllErrors()
					;
		if (ListUtil.size(errDetails) <= 0) {
			return errList;
		}

		for (ObjectError err : errDetails) {
			String	objName=err.getObjectName();
			String	objError=StringUtil.trimWithEllipsis(err.toString(), MAX_ERROR_TEXT_LENGTH);
			OperationUtils.addNameValuePair(errList, objName, objError);
		}

		return errList;
	}

	static final boolean collectExtraInformation () {
        return FrameBuilder.OperationCollectionLevel.HIGH.equals(configuration.getCollectionLevel());
    }
}
