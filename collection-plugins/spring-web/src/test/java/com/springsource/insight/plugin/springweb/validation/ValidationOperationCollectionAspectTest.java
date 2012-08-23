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

package com.springsource.insight.plugin.springweb.validation;

import org.junit.Test;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

public class ValidationOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
    public ValidationOperationCollectionAspectTest () {
    	super();
    }

    @Test
    public void validationCollected() {
        ExampleValidator validator = new ExampleValidator();
        
        validator.validate(new Object(), null);
        
        Operation   operation=getLastEntered();
        assertEquals("ExampleValidator#validate", operation.getLabel());
    }
    
    @Override
    public OperationCollectionAspectSupport getAspect() {
        return ValidationOperationCollectionAspect.aspectOf();
    }
    
    static class ExampleValidator implements Validator {
    	ExampleValidator () {
    		super();
    	}

        public boolean supports(Class<?> clazz) {
            return false;
        }

        public void validate(Object target, Errors errors) {
        	// ignored
        }
    }
}
