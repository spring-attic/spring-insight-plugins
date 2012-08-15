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

package com.springsource.insight.plugin.springweb.view.render;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.view.JstlView;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;

public class ViewRenderOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	public ViewRenderOperationCollectionAspectTest () {
		super();
	}

    @Test
    public void viewRenderMonitored() throws Exception {
        JstlView testUrlBasedView = new JstlView("myjsp.jsp");
        
        testUrlBasedView.setServletContext(new MockServletContext());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(RequestContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE, 
                             new GenericWebApplicationContext());
        Map<String, Object> model = new HashMap<String, Object>();
        testUrlBasedView.render(model, 
                                request, 
                                new MockHttpServletResponse());

        Operation   operation=getLastEntered();
        assertEquals("Render view \"myjsp.jsp\"", operation.getLabel());
        assertEquals("org.springframework.web.servlet.view.JstlView", operation.get("viewType"));
        assertEquals("text/html;charset=ISO-8859-1", operation.get("contentType"));
        assertEquals(0, operation.get("model", OperationMap.class).size());
    }
    
    @Override
    public OperationCollectionAspectSupport getAspect() {
        return ViewRenderOperationCollectionAspect.aspectOf();
    }
    

}
