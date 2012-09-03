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

package com.springsource.insight.plugin.portlet;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;

/**
 * 
 */
public abstract class GenericOperationCollectionTestSupport extends OperationCollectionAspectTestSupport {
	protected static ExamplePortletTester tester;
	protected GenericOperationCollectionTestSupport () {
		super();
	}
	
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

	protected Operation validate(OperationCollectionTypes collType, String mode) {
		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();
		assertNotNull("No operation data is intercepted",op);

		assertEquals("Invalid operation type", collType.type, op.getType());

		String portletName=op.get("name", String.class);
		assertEquals("Invalid portlet name", ExamplePortlet.NAME, portletName);
		
		String portletMode=op.get("mode", String.class);
		assertEquals("Invalid portlet mode", mode, portletMode);

		String	uri=op.get(OperationFields.URI, String.class);
		assertEquals("Mismatched URI", ExamplePortletTester.TEST_URL, uri);
		return op;
	}
}
