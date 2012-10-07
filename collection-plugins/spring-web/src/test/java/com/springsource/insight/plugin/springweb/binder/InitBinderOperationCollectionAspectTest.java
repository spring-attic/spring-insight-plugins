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

import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.context.request.WebRequest;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.util.ArrayUtil;

public class InitBinderOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
    private static final ExampleController testController=new ExampleController();
    
    public InitBinderOperationCollectionAspectTest () {
    	super();
    }
    
    @Test
    public void testInitBinderPickedCorrectlyFirstParam() {
    	WebDataBinder	dataBinder=new WebDataBinder(null);
        testController.initBinderFirstParam(dataBinder);
        assertDataBinderOperation(dataBinder);
    }

    @Test
    public void testInitBinderPickedCorrectlySecondParam() {
    	WebDataBinder	dataBinder=new WebDataBinder(this);
        testController.initBinderSecondParam(null, dataBinder);
        assertDataBinderOperation(dataBinder);
    }
    
    @Test 
    public void testInitBinderObjectNameCorrectlyCollectedWhenSpecified() {
    	WebDataBinder	dataBinder=new WebDataBinder(this, "testInitBinderObjectNameCorrectlyCollectedWhenSpecified");
        testController.initBinderFirstParam(dataBinder);
        assertDataBinderOperation(dataBinder);
    }

    @Override
    public InitBinderOperationCollectionAspect getAspect() {
        return InitBinderOperationCollectionAspect.aspectOf();
    }

    private Operation assertDataBinderOperation (DataBinder dataBinder) {
        Operation op = getLastEntered();
        assertNotNull("No operation", op);
        assertEquals("Mismatched type", InitBinderOperationCollectionAspect.TYPE, op.getType());
        assertEquals("Mismatched object name", dataBinder.getObjectName(), op.get(InitBinderOperationFinalizer.OBJECT_NAME, String.class));

        Object	target=dataBinder.getTarget();
        String	expected=(target == null) ? InitBinderOperationFinalizer.UNKNOWN_TARGET_TYPE : target.getClass().getName();
        assertEquals("Mismatched target type", expected, op.get(InitBinderOperationFinalizer.TARGET_TYPE, String.class));
        assertDataBinderFields(op, InitBinderOperationFinalizer.ALLOWED_FIELDS_LIST, dataBinder.getAllowedFields());
        assertDataBinderFields(op, InitBinderOperationFinalizer.DISALLOWED_FIELDS_LIST, dataBinder.getDisallowedFields());
        assertDataBinderFields(op, InitBinderOperationFinalizer.REQUIRED_FIELDS_LIST, dataBinder.getRequiredFields());

        return op;
    }

    private static OperationList assertDataBinderFields (Operation op, String key, String ... names) {
    	return assertDataBinderFields(op.get(key, OperationList.class), key, names);
    }

    private static OperationList assertDataBinderFields (OperationList list, String key, String ... names) {
    	assertNotNull(key + ": no list", list);
    	assertEquals(key + ": mismatched length", ArrayUtil.length(names), list.size());

    	for (int index=0; index < list.size(); index++) {
    		String	expected=names[index], actual=list.get(index, String.class);
    		assertEquals(key + ": mismatched value at index=" + index, expected, actual);
    	}

    	return list;
    }

    @Controller
    static class ExampleController {
    	ExampleController () {
    		super();
    	}

        @InitBinder
        public void initBinderFirstParam(WebDataBinder dataBinder) {
            dataBinder.setAllowedFields(new String[]{"allowed1", "allowed2"});
            dataBinder.setRequiredFields(new String[]{"required1", "required2"});
            dataBinder.setDisallowedFields(new String[]{"disAllowed1", "disAllowed2"});
        }

        @InitBinder
        public void initBinderSecondParam(WebRequest wr, WebDataBinder dataBinder) {
            dataBinder.setAllowedFields(new String[]{"allowed1", "allowed2"});
        }
    }
}
