/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.insight.plugin.springweb.request;

import static com.springsource.insight.intercept.operation.OperationFields.EXCEPTION;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.util.NestedServletException;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;

public class WebRequestOperationCollectionAspectTest
        extends OperationCollectionAspectTestSupport {
    private DispatcherServlet servlet;
    private MockServletConfig config;
    private MockHttpServletResponse response;

    public WebRequestOperationCollectionAspectTest() {
        super();
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();

        try {
            servlet = new DispatcherServlet();
            servlet.setContextClass(MyContext.class);
            config = new MockServletConfig(new MockServletContext(), "simple");
            servlet.init(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        response = new MockHttpServletResponse();
    }

    @Test
    public void doService_throwsException() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(config.getServletContext(),
                "GET", "/fail");
        try {
            servlet.service(request, response);
            fail("Should have failed");
        } catch (NestedServletException e) {
            // expected ignored
        }

        Operation operation = getLastEntered();
        assertEquals("Spring Web Dispatch", operation.getLabel());
        assertTrue(operation.get("error", Boolean.class).booleanValue());
        assertTrue(operation.get(EXCEPTION, String.class).startsWith("java.lang.RuntimeException: El-Kabong"));
        assertTrue(operation.get(EXCEPTION, String.class).contains("throwExceptionFromMethod"));
    }

    @Test
    public void doService() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(config.getServletContext(),
                "GET", "/success");
        servlet.service(request, response);

        Operation operation = getLastEntered();
        assertEquals("Spring Web Dispatch", operation.getLabel());
        assertEquals("GET", operation.get("method"));
        assertEquals("/success", operation.get(OperationFields.URI));
        assertFalse(operation.get("error", Boolean.class).booleanValue());
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return WebRequestOperationCollectionAspect.aspectOf();
    }

    public static class MyContext extends StaticWebApplicationContext {
        @Override
        public void refresh() {
            registerSingleton("/success", MyController.class);
            registerSingleton("/fail", MyController.class);
            super.refresh();
        }
    }

    public static class MyController implements Controller {
        private void throwExceptionFromMethod() {
            throw new RuntimeException("El-Kabong");
        }

        public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
            if (request.getRequestURI().equals("/success")) {
                // Yay!
            } else {
                throwExceptionFromMethod();
            }
            return null;
        }
    }
}
