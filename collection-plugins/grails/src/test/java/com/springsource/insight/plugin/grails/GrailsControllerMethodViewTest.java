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

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.View;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;

@ContextConfiguration(locations = { "classpath:META-INF/insight-plugin-grails.xml",
                                    "classpath:META-INF/test-app-context.xml" }, 
                      loader = WebApplicationContextLoader.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class GrailsControllerMethodViewTest {

    protected MockHttpServletRequest request;
    protected MockHttpServletResponse response;

    private final HashMap<String, Object> model = new HashMap<String, Object>();

    @Autowired
    @Qualifier("operation.grails_controller_method")
    private View local;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }
    
    @Test
    public void testLocalViewWithStatus() throws Exception {
        Operation operation = new Operation()
            .put("requestMethod", "PUT")
            .put("requestUri", "/my/uri")
            .put("actionName", "actionName");
        OperationList actionParams = operation.createList("actionParams");
        actionParams.createMap().put("key", "keyA").put("value", "valA");
        actionParams.createMap().put("key", "keyB").put("value", "valB");
        actionParams.createMap().put("key", "keyC").put("value", "valC");
        
        model.put("operation", operation.asMap());
        local.render(model, request, response);
        String content = response.getContentAsString();
        //System.err.println(content);
        int keyAindex = content.indexOf("<td>keyA</td>");
        int keyBindex = content.indexOf("<td>keyB</td>");        
        assertTrue(keyAindex >= 0 && keyBindex >=0 && keyBindex > keyAindex);
        
        assertTrue(content.contains("PUT /my/uri"));
    }    
}

