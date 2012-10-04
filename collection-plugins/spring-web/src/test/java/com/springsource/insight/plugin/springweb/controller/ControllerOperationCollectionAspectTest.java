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

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.util.MapUtil;

public class ControllerOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	public ControllerOperationCollectionAspectTest () {
		super();
	}

    @Test
    public void testControllerMonitored() {
        ExampleController testController = new ExampleController();
        testController.example();
        
        Operation   op=assertControllerOperation();
        assertNullValue(ControllerOperationCollector.RETURN_VALUE_MODEL_MAP, op.get(ControllerOperationCollector.RETURN_VALUE_MODEL_MAP, OperationMap.class));
        assertNullValue(ControllerOperationCollectionAspect.MODEL_ARGUMENT_NAME, op.get(ControllerOperationCollectionAspect.MODEL_ARGUMENT_NAME, OperationMap.class));
    }

    @Test
    public void testReturnModelValue () {
    	ExampleController controller=createTestExampleController("testReturnModelValue");
    	controller.returnModelValue();
    	assertEncodeModelValues(controller);
    }

    @Test
    public void testReturnModelMapValue () {
    	ExampleController controller=createTestExampleController("testReturnModelMapValue");
    	controller.returnModelMapValue();
    	assertEncodeModelValues(controller);
    }

    @Test
    public void testReturnModelAndViewValue () {
    	ExampleController controller=createTestExampleController("testReturnModelAndViewValue");
    	controller.returnModelAndViewValue();

    	Operation	op=assertEncodeModelValues(controller);
    	assertControllerView(op, controller);
    }

    @Test
    public void testReturnMapValue () {
    	ExampleController controller=createTestExampleController("testReturnMapValue");
    	controller.returnMapValue();
    	assertEncodeModelValues(controller);
    }

    @Test
    public void testWithModelArgument () {
    	ExampleController 	controller=createTestExampleController("testWithModelArgument");
    	Map<String,?>		argModel=createTestArgumentModelMap("testWithModelArgument");
    	controller.withModelArgument(new ExtendedModelMap().addAllAttributes(argModel));
    	assertEncodeModelArgValues(argModel);
    }

    @Test
    public void testWithModelMapArgument () {
    	ExampleController 	controller=createTestExampleController("testWithModelMapArgument");
    	Map<String,?>		argModel=createTestArgumentModelMap("testWithModelMapArgument");
    	controller.withModelMapArgument(new ModelMap().addAllAttributes(argModel));
    	assertEncodeModelArgValues(argModel);
    }

    @Test
    public void testWithSimpleMapArgument () {
    	ExampleController 	controller=createTestExampleController("testWithSimpleMapArgument");
    	Map<String,?>		argModel=createTestArgumentModelMap("testWithSimpleMapArgument");
    	controller.withSimpleMapArgument(argModel);
    	assertEncodeModelArgValues(argModel);
    }

    @Test
    public void testReturnModelAndViewValueWithModelArgument () {
    	ExampleController 	controller=createTestExampleController("testReturnModelAndViewValue");
    	Map<String,?>		argModel=createTestArgumentModelMap("testReturnModelAndViewValue");
    	controller.returnModelAndViewValueWithModelArgument(new ExtendedModelMap().addAllAttributes(argModel));

    	Operation	op=assertEncodeModelValues(controller);
    	assertEncodeModelArgValues(op, argModel);
    	assertControllerView(op, controller);
    }

    @Test
    public void testReturnView () {
    	ExampleController 	controller=createTestExampleController("testReturnView");
    	View				view=controller.returnView();
    	assertControllerView(view.getClass().getSimpleName());
    }

    @Test
    public void testReturnViewName () {
    	ExampleController 	controller=createTestExampleController("testReturnViewName");
    	assertControllerView(controller.returnViewName());
    }

    private ExampleController createTestExampleController (final String testName) {
    	return new ExampleController(createTestReturnModelMap(testName), testName);
    }

    private Map<String,Object> createTestArgumentModelMap (final String testName) {
    	return createTestModelMap(testName + "[" + ControllerOperationCollectionAspect.MODEL_ARGUMENT_NAME + "]");
    }

    private Map<String,Object> createTestReturnModelMap (final String testName) {
    	return createTestModelMap(testName + "[" + ControllerOperationCollector.RETURN_VALUE_MODEL_MAP + "]");
    }

    private Map<String,Object> createTestModelMap (final String testName) {
    	return new TreeMap<String, Object>() {
			private static final long serialVersionUID = 1L;

			{
				put("curDate", new Date());
				put("nanoTime", Long.valueOf(System.nanoTime()));
				put("testName", testName);
				put("boolValue", Boolean.valueOf((System.currentTimeMillis() & 0x01L) == 0L));
    		}
    	};
    }

    private Operation assertControllerView (String expected) {
    	Operation	op=assertControllerOperation();
    	assertControllerView(op, expected);
    	return op;
    }

	private static String assertControllerView(Operation op, ExampleController controller) {
		return assertControllerView(op, controller.returnView);
	}

	private static String assertControllerView(Operation op, String expected) {
		String	viewName=op.get(ControllerOperationCollector.RETURN_VALUE_VIEW_NAME, String.class);
		assertEquals("Mismatched view name", expected, viewName);
		return viewName;
	}
    private Operation assertEncodeModelArgValues (Map<String,?> argModel) {
    	Operation op=getLastEntered();
    	assertNotNull("No operation entered", op);
    	assertEncodeModelArgValues(op, argModel);
    	return op;
    }

    private OperationMap assertEncodeModelArgValues (Operation op, Map<String,?> argModel) {
    	return assertEncodeModelValues(op, ControllerOperationCollectionAspect.MODEL_ARGUMENT_NAME, argModel);
    }

    private Operation assertEncodeModelValues (ExampleController controller) {
    	return assertEncodeModelValues(ControllerOperationCollector.RETURN_VALUE_MODEL_MAP, controller.returnModel);
    }

    private Operation assertEncodeModelValues (String mapName, Map<String,?> expected) {
    	Operation	op=assertControllerOperation();
    	assertEncodeModelValues(op, mapName , expected);
    	return op;
    }

    private Operation assertControllerOperation () {
    	Operation   op=getLastEntered();
    	assertNotNull("No operation entered", op);
    	assertEquals("Mismatched operation type", ControllerEndPointAnalyzer.CONTROLLER_METHOD_TYPE, op.getType());
    	return op;
    }

    static OperationMap assertEncodeModelValues (Operation op, String mapName, Map<String,?> expected) {
    	OperationMap	map=op.get(mapName, OperationMap.class);
    	assertNotNull(mapName + ": missing encoding", map);
    	return assertEncodeModelValues(map, mapName, expected);
    }

    static OperationMap assertEncodeModelValues (OperationMap map, String mapName, Map<String,?> expected) {
    	assertEquals(mapName + ": Mismatched size", MapUtil.size(expected), map.size());
    	
    	for (Map.Entry<String,?> me : map.entrySet()) {
    		String	key=me.getKey();
    		Object	actualValue=me.getValue();
    		Object	expectedValue=expected.get(key);
    		assertEquals(mapName + ": Mismatched value for " + key, expectedValue, actualValue);
    	}

    	return map;
    }

    @Controller
    static class ExampleController {
    	final Map<String,?>	returnModel;
    	final String returnView;

    	ExampleController () {
    		this(Collections.<String,Object>emptyMap());
    	}

    	ExampleController (Map<String,?> outgoingModel) {
    		this(outgoingModel, "");
    	}

    	ExampleController (Map<String,?> outgoingModel, String outgoingView) {
    		returnModel = outgoingModel;
    		returnView = outgoingView;
    	}

        @RequestMapping(value="/example")
        public void example() {
        	// do nothing
        }

        @RequestMapping(value="/returnModel")
        public Model returnModelValue () {
        	return new ExtendedModelMap().addAllAttributes(returnModel);
        }

        @RequestMapping(value="/returnModelMapValue")
        public ModelMap returnModelMapValue () {
        	return new ModelMap().addAllAttributes(returnModel);
        }

        @RequestMapping(value="/returnModelAndViewValue")
        public ModelAndView returnModelAndViewValue () {
        	return new ModelAndView(returnView, returnModel);
        }

        @RequestMapping(value="/returnModelAndViewValueWithModelArgument")
        public ModelAndView returnModelAndViewValueWithModelArgument (Model model) {
        	assertNotNull("Missing model value", model);
        	return new ModelAndView(returnView, returnModel);
        }

        @RequestMapping(value="/returnMapValue")
        public Map<String,?> returnMapValue () {
        	return returnModel;
        }

        @RequestMapping(value="/withModelArgument")
        public void withModelArgument (Model model) {
        	assertNotNull("Missing model value", model);
        }

        @RequestMapping(value="/withModelMapArgument")
        public void withModelMapArgument (ModelMap model) {
        	assertNotNull("Missing model value", model);
        }

        @RequestMapping(value="/withSimpleMapArgument")
        public void withSimpleMapArgument (Map<String,?> model) {
        	assertNotNull("Missing model value", model);
        }
        
        @RequestMapping(value="/returnView")
        public View returnView () {
        	return new ExampleView("java/x-class");
        }

        @RequestMapping(value="/returnViewName")
        public String returnViewName () {
        	return returnView;
        }
    }

    static class ExampleView implements View {
    	final String	contentType;

    	ExampleView () {
    		this("");
    	}

    	ExampleView (String ct) {
    		contentType = ct;
    	}

		public String getContentType() {
			return contentType;
		}

		public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
			assertNotNull("No model", model);
			assertNotNull("No request", request);
			assertNotNull("No response", response);
		}
    	
    }
    @Override
    public ControllerOperationCollectionAspect getAspect() {
        return ControllerOperationCollectionAspect.aspectOf();
    }

}
