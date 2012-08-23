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

package com.springsource.insight.plugin.springweb.modelattribute;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

public class ModelAttributeOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	public ModelAttributeOperationCollectionAspectTest () {
		super();
	}

    @Test
    public void modelAttributeMethodWithComplexReturnTypeMonitored() {
        ExampleController testController = new ExampleController();
        testController.namedAccount_specialAccount();
        
        Operation   operation=getLastEntered();
        assertEquals(Account.class.getName(), operation.get("value"));
        assertEquals("specialAccount", operation.get("modelAttributeName"));
    }
    
    @Test
    public void modelAttributeMethodWithSimpleReturnTypeMonitored() {
        ExampleController testController = new ExampleController();
        testController.namedString_specialString();
        
        Operation   operation=getLastEntered();
        assertEquals("testString", operation.get("value"));
        assertEquals("specialString", operation.get("modelAttributeName"));
    }

    @Test
    public void modelAttributeMethodUnnamedScalarAttributeMonitored() {
        ExampleController testController = new ExampleController();
        testController.unnamedAttributeAccount();
        
        Operation   operation=getLastEntered();
        assertEquals(Account.class.getName(),
                     operation.get("value"));
        assertEquals("account", operation.get("modelAttributeName"));
    }

    @Test
    public void modelAttributeMethodWithUnnamedListAttributeMonitored() {
        ExampleController testController = new ExampleController();
        testController.unnamedAttributeListAccount();
        
        Operation   operation=getLastEntered();
        assertEquals("java.util.ArrayList", operation.get("value"));
        assertEquals("accountList", operation.get("modelAttributeName"));
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return ModelAttributeOperationCollectionAspect.aspectOf();
    }

    @Controller
    static class ExampleController {
    	ExampleController () {
    		super();
    	}

        @ModelAttribute("specialAccount")
        public Account namedAccount_specialAccount() {
            return new Account();
        }

        @ModelAttribute("specialString")
        public String namedString_specialString() {
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
