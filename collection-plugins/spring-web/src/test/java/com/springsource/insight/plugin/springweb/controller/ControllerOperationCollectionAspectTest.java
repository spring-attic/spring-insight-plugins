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

package com.springsource.insight.plugin.springweb.controller;

import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

public class ControllerOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	public ControllerOperationCollectionAspectTest () {
		super();
	}

    @Test
    public void controllerMonitored() {
        ExampleController testController = new ExampleController();
        testController.example();
        
        Operation   operation=getLastEntered();
        assertEquals(ControllerEndPointAnalyzer.CONTROLLER_METHOD_TYPE, operation.getType());
    }
    
    @Controller
    static class ExampleController {
        @RequestMapping(value="/example")
        public void example() {
        	// do nothing
        }
    }
    
    @Override
    public OperationCollectionAspectSupport getAspect() {
        return ControllerOperationCollectionAspect.aspectOf();
    }

}
