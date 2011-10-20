/**
 * Copyright 2009-2010 the original author or authors.
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

package com.springsource.insight.plugin.integration;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.integration.Message;
import org.springframework.integration.message.GenericMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.springsource.insight.idk.AbstractOperationViewTest;
import com.springsource.insight.idk.WebApplicationContextLoader;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.operation.SourceCodeLocation;

import static org.junit.Assert.*;

/**
 * This tests the view-rendering portion of the Spring Integration plugin. 
 * 
 * @author Gary Russell
 */
@ContextConfiguration(locations = { "classpath:META-INF/insight-plugin-integration-plugin.xml", 
                                    "classpath:META-INF/test-app-context.xml" },
                      loader = WebApplicationContextLoader.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class IntegrationOperationViewTest 
    extends AbstractOperationViewTest
{    
    public IntegrationOperationViewTest() {
        super(OperationType.valueOf("integration_operation"));
    }
    
    /**
     * This tests that Integration Operations can be rendered via the FreeMarker
     * template.
     */
    @Test
    public void testLocalViewWithStatus() throws Exception {
        SourceCodeLocation scl = new SourceCodeLocation("MyClass", "methodName", 45);
        Message<String> message = new GenericMessage<String>("TestMessage");
        String beanName="testChannel";
        String beanType="Channel";
        Operation operation = new Operation()
			.type(getOperationType())
			.sourceCodeLocation(scl)
			.label(beanName + "#" + scl.getMethodName() + "()")
			.put("siComponentType", beanType)
			.put("beanName", beanName)
			.put("payloadType", "java.lang.String")
			.put("idHeader", message.getHeaders().getId().toString());
            
        String content = getRenderingOf(operation);
        System.err.println(content);
        
        // Simply test for some expected contents within the HTML.
        assertTrue(content.contains("java.lang.String"));
        assertTrue(content.contains(message.getHeaders().getId().toString()));
        assertTrue(content.contains("Channel"));
        assertTrue(content.contains("testChan"));    
        assertFalse(content.contains("Result Message ID"));
    }    

}

