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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.SourceCodeLocation;

public class LegacyControllerOperationCollectionAspectTest extends AbstractControllerOperationCollectionAspectTestSupport {
    public LegacyControllerOperationCollectionAspectTest() {
        super(true);
    }

    @Test
    public void testControllerMonitored() {
        ExampleController testController = new ExampleController(createTestModelMap("testControllerMonitored"), "testControllerMonitored");
        testController.handleRequest(null, null);

        Operation op = assertEncodeReturnModelValues(testController);
        assertControllerView(op, testController.returnView);

        SourceCodeLocation source = op.getSourceCodeLocation();
        assertEquals("Mismatched source class", ExampleController.class.getName(), source.getClassName());
        assertEquals("Mismatched method name", "handleRequest", source.getMethodName());
    }

    static class ExampleControllerBase extends TestSupportController implements Controller {
        public ExampleControllerBase(Map<String, ?> outgoingModel, String outgoingView) {
            super(outgoingModel, outgoingView);
        }

        public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
            return new ModelAndView(returnView, returnModel);
        }
    }

    static class ExampleController extends ExampleControllerBase {
        public ExampleController(Map<String, ?> outgoingModel, String outgoingView) {
            super(outgoingModel, outgoingView);
        }
    }

    @Override
    public LegacyControllerOperationCollectionAspect getAspect() {
        return LegacyControllerOperationCollectionAspect.aspectOf();
    }
}
