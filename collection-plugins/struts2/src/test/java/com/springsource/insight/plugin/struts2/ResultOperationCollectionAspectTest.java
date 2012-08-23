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

package com.springsource.insight.plugin.struts2;


import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;

/**
 * Test Collection operation for Struts2 overall action result
 */
public class ResultOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	
	@Test
	/**
	 * Tests success action without validation
	 */
	public void test1() throws Exception {
		// Step 1: Execute test
		Struts2Tests.getInstance().testExecutePasses();
	
		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();
		assertNotNull("No operation data is intercepted",op);

		// Step 3:  Validate
		assertTrue("Invalid operation type: "+op.getType().getName()+", expected: "+OperationCollectionTypes.RESULT_TYPE.getValue(),
					op.getType().equals(OperationCollectionTypes.RESULT_TYPE.type));
		
		assertTrue("Invalid result code: "+op.get("resultCode")+", expected: success", "success".equals(op.get("resultCode")));
		assertTrue("Invalid result view: "+op.get("view")+", expected: /thankyou.jsp", "/thankyou.jsp".equals(op.get("view")));
	}
	
	@Test
	/**
	 * Tests input action with validation errors
	 */
	public void test2() throws Exception {
		// Step 1: Execute test
		Struts2Tests.getInstance().testExecuteValidationFailsMissingFirstName();
	
		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();
		assertNotNull("No operation data is intercepted",op);

		// Step 3:  Validate
		assertTrue("Invalid operation type: "+op.getType().getName()+", expected: "+OperationCollectionTypes.RESULT_TYPE.getValue(),
					op.getType().equals(OperationCollectionTypes.RESULT_TYPE.type));
		
		assertTrue("Invalid result code: "+op.get("resultCode")+", expected: input", "input".equals(op.get("resultCode")));
		assertTrue("Invalid result view: "+op.get("view")+", expected: /register.jsp", "/register.jsp".equals(op.get("view")));
		
		OperationMap errs=(OperationMap) op.get("errs");
		assertTrue("Invalid fields validation errors", errs!=null && errs.size()>0 && errs.get("personBean.firstName")!=null);
	}
	
	@Override
	public OperationCollectionAspectSupport getAspect() {
		return ResultOperationCollectionAspect.aspectOf();
	}
}
