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

import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

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
        
        Operation   op=getLastEntered();
        assertNotNull("No operation entered", op);
        assertEquals(ControllerEndPointAnalyzer.CONTROLLER_METHOD_TYPE, op.getType());

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
    	assertEncodeModelValues(controller);
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
    }

    private ExampleController createTestExampleController (final String testName) {
    	return new ExampleController(createTestReturnModelMap(testName));
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
    	Operation	op=getLastEntered();
    	assertNotNull(mapName + ": no entered operation", op);
    	assertEncodeModelValues(op, mapName , expected);
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

    	ExampleController () {
    		this(Collections.<String,Object>emptyMap());
    	}

    	ExampleController (Map<String,?> outgoingModel) {
    		returnModel = outgoingModel;
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
        	return new ModelAndView("returnModelAndViewValue", returnModel);
        }

        @RequestMapping(value="/returnModelAndViewValueWithModelArgument")
        public ModelAndView returnModelAndViewValueWithModelArgument (Model model) {
        	assertNotNull("Missing model value", model);
        	return new ModelAndView("returnModelAndViewValue", returnModel);
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
    }
    
    @Override
    public ControllerOperationCollectionAspect getAspect() {
        return ControllerOperationCollectionAspect.aspectOf();
    }

}
