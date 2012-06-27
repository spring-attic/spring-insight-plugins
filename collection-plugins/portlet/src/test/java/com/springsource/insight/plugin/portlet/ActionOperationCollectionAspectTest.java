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
package com.springsource.insight.plugin.portlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

public class ActionOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	private static ExamplePortletTester tester;
	
	@BeforeClass
    public static void setUpClass() throws Exception {
        // Code executed before the first test method
		tester=new ExamplePortletTester();
		tester.setUp();
    }
	
	@AfterClass
    public static void tearDownClass() throws Exception {
        // Code executed after the last test method
		tester.tearDown();
    }
	
	/*
	 * tests view render 
	 */
	@Test
	public void testView() throws Exception {
		// Step 1: Execute test
		tester.doAction();

		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();
		assertNotNull("No operation data is intercepted",op);

		// Step 3:  Validate
		assertEquals("Invalid operation type", OperationCollectionTypes.ACTION_TYPE.type, op.getType());

		String portletName=op.get("name", String.class);
		assertEquals("Invalid portlet name", ExamplePortlet.NAME, portletName);
		
		String portletMode=op.get("mode", String.class);
		assertEquals("Invalid portlet mode", "view", portletMode);
	}
	
	@Override
	public OperationCollectionAspectSupport getAspect() {
		return ActionOperationCollectionAspect.aspectOf();
	}
}
