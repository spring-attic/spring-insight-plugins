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

package com.springsource.insight.plugin.grails;

import static com.springsource.insight.intercept.operation.OperationFields.EXCEPTION;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import groovy.lang.Closure;
import groovy.lang.GroovyObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.grails.commons.GrailsControllerClass;
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest;
import org.codehaus.groovy.grails.web.servlet.mvc.SimpleGrailsControllerHelper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.servlet.ModelAndView;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.SourceCodeLocation;

@SuppressWarnings("unchecked")
public class GrailsControllerOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private GrailsWebRequest mockRequest;

    public GrailsControllerOperationCollectionAspectTest () {
    	super();
    }

    @Override
	@Before
    public void setUp() {
        super.setUp();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        mockRequest = new GrailsWebRequest(request, response, new MockServletContext()); 
    }
    
    @Test
    public void controllerMonitored() throws Exception {
        Map<String,String> actionParams = new HashMap<String,String>();
        actionParams.put("param1", "val1");
        actionParams.put("param2", "val2");        
        ExampleHelper testHelper = new ExampleHelper("", actionParams);

        request.setMethod("PUT");
        ModelAndView model = testHelper.handleURI("/my/uri", mockRequest, new HashMap<String,String>());
        assertEquals(Boolean.TRUE, model.getModelMap().get("success"));
        Operation op = getLastEntered();
        assertEquals("MyClass#gettinAction", op.getLabel());
        SourceCodeLocation sourceCode = op.getSourceCodeLocation();
        assertEquals("my.controller.MyClass", sourceCode.getClassName());
        assertEquals("gettinAction", sourceCode.getMethodName());
        assertEquals(1, sourceCode.getLineNumber());
        assertEquals("param1", op.get("actionParams", OperationList.class).get(0, OperationMap.class).get("key"));
        assertEquals("/my/uri", op.get("requestUri"));
        assertEquals("PUT", op.get("requestMethod"));
    }

    @Test
    public void controllerMonitored_exceptionBeforeActionOrController() throws Exception {
        ExampleHelper testHelper = new ExampleHelper("blowUpBeforeController", Collections.EMPTY_MAP);
        try {
            testHelper.handleURI("uri", mockRequest, new HashMap<String,String>());
            fail("Expected exception");
        } catch (RuntimeException e) {
        	// ignored
        }

        Operation op = getLastEntered();
        assertEquals("UnknownController#unknownAction", op.getLabel());
        assertNotNull(op.get(EXCEPTION));
    }

    @Test
    public void controllerMonitored_exceptionButControllerIsKnown() throws Exception {
        ExampleHelper testHelper = new ExampleHelper("blowUpAfterController", Collections.EMPTY_MAP);
        try {
            testHelper.handleURI("uri", mockRequest, new HashMap<String,String>());
            fail("Expected exception");
        } catch (RuntimeException e) {
        	// ignored
        }

        Operation op = getLastEntered();
        assertEquals("MyClass#unknownAction", op.getLabel());
        assertNotNull(op.get(EXCEPTION));
    }

    @Test
    public void controllerMonitored_exceptionButControllerAndActionAreKnown() throws Exception {
        ExampleHelper testHelper = new ExampleHelper("blowUpAfterAction", Collections.EMPTY_MAP);
        try {
            testHelper.handleURI("uri", mockRequest, new HashMap<String,String>());
            fail("Expected exception");
        } catch (RuntimeException e) {
        	// ignored
        }

        Operation op = getLastEntered();
        assertEquals("MyClass#gettinAction", op.getLabel());
        assertNotNull(op.get(EXCEPTION));
    }

    /**
     * This helper acts similarly to what the aspects are expecting of the
     * {@link SimpleGrailsControllerHelper}.
     * 
     * In addition, this class is able to throw exceptions at various location,
     * to allow testing of failure scenarios.
     */
    private static class ExampleHelper implements GrailsControllerHelper {
        private String whenToBlowUp;
        private Map<String,String> actionParams;

        public ExampleHelper() {
            this("", Collections.<String,String>emptyMap());
        }

        ExampleHelper(String blowUp, Map<String,String> params) {
            this.whenToBlowUp = blowUp;
            this.actionParams = params;
        }

        public GrailsControllerClass getControllerClassByName(String arg) {
            return null;
        }

        public GrailsControllerClass getControllerClassByURI(String arg0) {
            GrailsControllerClass res = mock(GrailsControllerClass.class);
            when(res.getFullName()).thenReturn("my.controller.MyClass");
            when(res.getShortName()).thenReturn("MyClass");
            return res;
        }

        public GroovyObject getControllerInstance(GrailsControllerClass arg) {
            return null;
        }

        public GrailsApplicationAttributes getGrailsAttributes() {
            return null;
        }

        public ServletContext getServletContext() {
            return null;
        }

        public Object handleAction(GroovyObject arg0, Closure arg1, HttpServletRequest arg2, HttpServletResponse arg3) {
            return null;
        }

        public Object handleAction(GroovyObject controller,
                                   Closure action,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   Map params)

        {
            return null;
        }

        public ModelAndView handleActionResponse(GroovyObject arg0, Object arg1, String arg2, String arg3) {
            return null;
        }

        public ModelAndView handleURI(String arg0, GrailsWebRequest arg1) {
            return null;
        }

        /**
         * This method is constructed very similarly to SimpleGrailsControllerHelper, since
         * we are interested at snooping in various parts of that method.
         */
        public ModelAndView handleURI(String uri, GrailsWebRequest webRequest, Map parms) {
            if (whenToBlowUp.equals("blowUpBeforeController")) {
                throw new RuntimeException("Kaboom");
            }

            GrailsControllerClass controllerClass = getControllerClassByURI(uri);
            assertNotNull(controllerClass);
            if (whenToBlowUp.equals("blowUpAfterController")) {
                throw new RuntimeException("Kaboom2");
            }

            webRequest.setActionName("gettinAction");
            
            GroovyObject controller = mock(GroovyObject.class);
            GrailsParameterMap paramsMap = new GrailsParameterMap(actionParams, new MockHttpServletRequest()); 
            when(controller.getProperty("params")).thenReturn(paramsMap);
            
            
            Closure action = mock(Closure.class);
            handleAction(controller, action, webRequest.getCurrentRequest(),
                         webRequest.getCurrentResponse(), new HashMap<String,String>());
            
            if (whenToBlowUp.equals("blowUpAfterAction")) {
                throw new RuntimeException("Kaboom3");
            }
            
            ModelAndView res = new ModelAndView();
            res.addObject("success", Boolean.TRUE);
            return res;
        }
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return GrailsControllerOperationCollectionAspect.aspectOf();
    }

}
