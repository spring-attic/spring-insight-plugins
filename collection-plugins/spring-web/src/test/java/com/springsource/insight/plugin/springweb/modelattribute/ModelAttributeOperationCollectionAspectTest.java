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

package com.springsource.insight.plugin.springweb.modelattribute;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

public class ModelAttributeOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
    private static final ExampleController testController = new ExampleController();

	public ModelAttributeOperationCollectionAspectTest () {
		super();
	}

    @Test
    public void testModelAttributeMethodWithComplexReturnTypeMonitored() {
        testController.namedAccountSpecialAccount();
        assertModelAttributeOperation("specialAccount", Account.class.getName());
    }
    
    @Test
    public void testModelAttributeMethodWithSimpleReturnTypeMonitored() {
        testController.namedStringSpecialString();
        assertModelAttributeOperation("specialString", "testString");
    }

    @Test
    public void testModelAttributeMethodUnnamedScalarAttributeMonitored() {
        testController.unnamedAttributeAccount();
        assertModelAttributeOperation("account", Account.class.getName());
    }

    @Test
    public void testModelAttributeMethodWithUnnamedListAttributeMonitored() {
        testController.unnamedAttributeListAccount();
        assertModelAttributeOperation("accountList", ArrayList.class.getName());
    }

    @Override
    public ModelAttributeOperationCollectionAspect getAspect() {
        return ModelAttributeOperationCollectionAspect.aspectOf();
    }

    private Operation assertModelAttributeOperation (String name, String value) {
    	Operation	op=getLastEntered();
    	assertNotNull("No extracted operation", op);
    	assertEquals("Mismatched operation type", ModelAttributeOperationCollector.TYPE, op.getType());
    	assertEquals("Mismatched attribute name", name, op.get(ModelAttributeOperationCollector.MODEL_ATTR_NAME, String.class));
    	assertEquals("Mismatched attribute value", value, op.get(ModelAttributeOperationCollector.MODEL_ATTR_VALUE, String.class));

    	return op;
    }

    @Controller
    static class ExampleController {
    	ExampleController () {
    		super();
    	}

        @ModelAttribute("specialAccount")
        public Account namedAccountSpecialAccount() {
            return new Account();
        }

        @ModelAttribute("specialString")
        public String namedStringSpecialString() {
            return "testString";
        }
        
        @ModelAttribute
        public Account unnamedAttributeAccount() {
            return new Account();
        }
        
        @ModelAttribute
        public List<Account> unnamedAttributeListAccount() {
            return new ArrayList<Account>();
        }
    }
    
    static class Account {
    	Account () {
    		super();
		}
    }
}
