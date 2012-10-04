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

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationUtils;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;

public class ValidationOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
    private static final ExampleValidator validator=new ExampleValidator();

    public ValidationOperationCollectionAspectTest () {
    	super();
    }

    @Test
    public void testValidationCollectedNoErrors() {
    	assertValidationOperation(createMockErrors("testValidationCollectedNoErrors", Collections.<ObjectError>emptyList()));
    }

    @Test
    public void testValidationCollectedWithErrors() {
    	assertValidationOperation(
    			createMockErrors("testValidationCollectedWithErrors", Arrays.asList(
    								new ObjectError("curDate", new Date().toString()),
    								new ObjectError("curTime", String.valueOf(System.currentTimeMillis())),
    								new ObjectError("className", getClass().getSimpleName())
    							)));
    }

    @Override
    public ValidationOperationCollectionAspect getAspect() {
        return ValidationOperationCollectionAspect.aspectOf();
    }

    private Operation assertValidationOperation (Errors errors) {
    	return assertValidationOperation(validator, errors);
    }

    private Operation assertValidationOperation (Object target, Errors errors) {
    	validator.validate(target, errors);

    	Operation	op=assertValidationOperation();
    	assertValidationErrors(op, errors);
    	return op;
    }

    private Operation assertValidationOperation () {
        Operation   op=getLastEntered();
        assertNotNull("No operation entered", op);
        assertEquals("Mismatched operation type", ValidationErrorsMetricsGenerator.TYPE, op.getType());
        assertEquals("Mismatched label", validator.getClass().getSimpleName() + "#validate", op.getLabel());
        return op;
    }

    private static OperationList assertValidationErrors (Operation op, Errors errors) {
    	int	numErrors=(errors == null) ? 0 : errors.getErrorCount();
    	assertEquals("Mismatched number of errors", Integer.valueOf(numErrors), op.get(ValidationJoinPointFinalizer.ERRORS_COUNT, Integer.class));

    	OperationList	errDetails=op.get(ValidationJoinPointFinalizer.ERRORS_LIST, OperationList.class);
    	if (numErrors > 0) {
    		assertValidationErrors(errDetails, errors);
    	} else {
    		assertNull("Unexpected errors details: " + errDetails, errDetails);
    	}

    	return errDetails;
    }

    private static OperationList assertValidationErrors (OperationList errDetails, Errors errors) {
    	assertEquals("Mismatched number of errors", errors.getErrorCount(), errDetails.size());
    	
    	List<? extends ObjectError>	errList=errors.getAllErrors();
    	for (int	index=0; index < errList.size(); index++) {
    		ObjectError		expected=errList.get(index);
    		OperationMap	actual=errDetails.get(index, OperationMap.class);
    		assertNotNull("Missing encoded value for error #" + index + ": " + expected, actual);

    		String	objName=actual.get(OperationUtils.NAME_KEY, String.class);
    		assertEquals("Mismatched object name at entry #" + index, expected.getObjectName(), objName);

    		String	errExpected=StringUtil.trimWithEllipsis(expected.toString(), ValidationJoinPointFinalizer.MAX_ERROR_TEXT_LENGTH);
    		String	errActual=actual.get(OperationUtils.VALUE_KEY, String.class);
    		assertEquals("Mismatched error text at entry #" + index, errExpected, errActual);
    	}

    	return errDetails;
    }

    @SuppressWarnings("boxing")
	static Errors createMockErrors (String objName, List<ObjectError> errList) {
    	Errors	errors=Mockito.mock(Errors.class);
    	int		numErrors=ListUtil.size(errList);
    	Mockito.when(errors.getErrorCount()).thenReturn(numErrors);
    	Mockito.when(errors.getAllErrors()).thenReturn(errList);
    	return errors;
    }

    static class ExampleValidator implements Validator {
    	ExampleValidator () {
    		super();
    	}

        public boolean supports(Class<?> clazz) {
            return (clazz != null);
        }

        public void validate(Object target, Errors errors) {
        	// ignored
        }
    }
}
