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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.SourceCodeLocation;

public class LegacyControllerOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	public LegacyControllerOperationCollectionAspectTest () {
		super();
	}

    @Test
    public void controllerMonitored() {
        ExampleController testController = new ExampleController();
        testController.handleRequest(null, null);

        Operation   op=getLastEntered();
        assertNotNull("No operation extracted", op);
        assertEquals("Mismatched type", ControllerEndPointAnalyzer.CONTROLLER_METHOD_TYPE, op.getType());

        SourceCodeLocation source = op.getSourceCodeLocation();
        assertEquals("Mismatched source class", ExampleController.class.getName(), source.getClassName());
        assertEquals("Mismatched method name", "handleRequest", source.getMethodName());
    }
    
    static class ExampleControllerBase implements Controller {
        public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
            return null;
        }
    }
    
    static class ExampleController extends ExampleControllerBase {
    	public ExampleController () {
    		super();
    	}
    }
    
    @Override
    public OperationCollectionAspectSupport getAspect() {
        return LegacyControllerOperationCollectionAspect.aspectOf();
    }
}
