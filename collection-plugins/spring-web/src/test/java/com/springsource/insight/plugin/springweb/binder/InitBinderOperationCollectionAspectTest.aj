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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.context.request.WebRequest;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

public aspect InitBinderOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
    private ExampleController testController;
    
    @Override
	@Before
    public void setUp() {
        super.setUp();
        testController = new ExampleController();
    }
    
    @Test
    public void initBinderPickedCorrectlyFirstParam() throws Exception {
        testController.initBinderFirstParam(new WebDataBinder(null));
        
        ArgumentCaptor<Operation> operationCaptor = ArgumentCaptor.forClass(Operation.class);
        verify(spiedOperationCollector).enter(operationCaptor.capture());
        Operation operation = operationCaptor.getValue();
        operation.finalizeConstruction();
        assertEquals(DataBinder.DEFAULT_OBJECT_NAME, operation.get("objectName"));
        assertEquals("unknown", operation.get("targetType"));
        assertEquals(Arrays.asList("allowed1", "allowed2"), operation.asMap().get("allowedFields"));
        assertEquals(Arrays.asList("required1", "required2"), operation.asMap().get("requiredFields"));
        assertEquals(Arrays.asList("disAllowed1", "disAllowed2"), operation.asMap().get("disallowedFields"));
    }
    
    @Test
    public void initBinderPickedCorrectlySecondParam() throws Exception {
        testController.initBinderSecondParam(null, new WebDataBinder(this));
        
        ArgumentCaptor<Operation> operationCaptor = ArgumentCaptor.forClass(Operation.class);
        verify(spiedOperationCollector).enter(operationCaptor.capture());
        Operation operation = operationCaptor.getValue();
        operation.finalizeConstruction();
        assertEquals(DataBinder.DEFAULT_OBJECT_NAME, operation.get("objectName"));
        assertEquals(getClass().getName(), operation.get("targetType"));
        assertEquals(Arrays.asList("allowed1", "allowed2"), operation.asMap().get("allowedFields"));
        assertEquals(Collections.emptyList(), operation.asMap().get("requiredFields"));
        assertEquals(Collections.emptyList(), operation.asMap().get("disallowedFields"));
    }
    
    @Test 
    public void initBinderObjectNameCorrectlyCollectedWhenSpecified() throws Exception {
        testController.initBinderFirstParam(new WebDataBinder(this, "testObjName"));
        
        ArgumentCaptor<Operation> operationCaptor = ArgumentCaptor.forClass(Operation.class);
        verify(spiedOperationCollector).enter(operationCaptor.capture());
        Operation operation = operationCaptor.getValue();
        operation.finalizeConstruction();
        assertEquals("testObjName", operation.get("objectName"));
        assertEquals(getClass().getName(), operation.get("targetType"));        
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return InitBinderOperationCollectionAspect.aspectOf();
    }

    @Controller
    static class ExampleController {
    	ExampleController () {
    		super();
    	}

        @InitBinder
        public void initBinderFirstParam(WebDataBinder dataBinder) throws Exception {
            dataBinder.setAllowedFields(new String[]{"allowed1", "allowed2"});
            dataBinder.setRequiredFields(new String[]{"required1", "required2"});
            dataBinder.setDisallowedFields(new String[]{"disAllowed1", "disAllowed2"});
        }

        @InitBinder
        public void initBinderSecondParam(WebRequest wr, WebDataBinder dataBinder) throws Exception {
            dataBinder.setAllowedFields(new String[]{"allowed1", "allowed2"});
        }
    }
}
