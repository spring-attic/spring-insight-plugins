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

package com.springsource.insight.plugin.struts2;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;

/**
 * Test Collection operation for Struts2 flow execution start 
 *
 */
public class StartOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	
	@Test
	/**
	 * tests action without validation
	 */
	public void test1() throws Exception {
		// Step 1: Execute test
		Struts2Tests.getInstance().testExecutePasses();

		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();
		assertNotNull("No operation data is intercepted",op);

		// Step 3:  Validate
		assertTrue("Invalid operation type: "+op.getType().getName()+", expected: "+OperationCollectionTypes.START_TYPE.getValue(),
					op.getType().equals(OperationCollectionTypes.START_TYPE.type));

		assertTrue("Invalid action name: "+op.get("actionName")+", expected: /register2", "/register2".equals(op.get("actionName")));
		
		OperationMap params=(OperationMap) op.get("params");
		assertTrue("Invalid request parameters", params!=null && params.size()>0 && params.get("personBean.lastName")!=null);
	}
	
	@Test
	/**
	 * tests action with validation
	 */
	public void test2() throws Exception {
		// Step 1: Execute test
		Struts2Tests.getInstance().testExecuteValidationFailsMissingFirstName();

		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();
		assertNotNull("No operation data is intercepted",op);

		// Step 3:  Validate
		assertTrue("Invalid operation type: "+op.getType().getName()+", expected: "+OperationCollectionTypes.START_TYPE.getValue(),
					op.getType().equals(OperationCollectionTypes.START_TYPE.type));
		
		assertTrue("Invalid action name: "+op.get("actionName")+", expected: /register", "/register".equals(op.get("actionName")));
		
		OperationMap params=(OperationMap) op.get("params");
		assertTrue("Invalid result parameters", params!=null && params.size()>0 && params.get("personBean.lastName")!=null);
	}

	@Override
	public OperationCollectionAspectSupport getAspect() {
		return StartOperationCollectionAspect.aspectOf();
	}
}
