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
package com.springsource.insight.plugin.jcr;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

public class LoginOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	@Override
	public OperationCollectionAspectSupport getAspect() {
		return LoginOperationCollectionAspect.aspectOf();
	}
	
	@Test
	public void test1() throws Exception {
		System.out.println("Running login test..");
		
		// Step 1: Execute test
		SimpleTests.getInstance().test();
	
		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();
		assertNotNull("No operation data is intercepted",op);

		// Step 3:  Validate
		assertTrue("Invalid operation type: "+op.getType().getName()+", expected: "+OperationCollectionTypes.LOGIN_TYPE.type,
							op.getType().equals(OperationCollectionTypes.LOGIN_TYPE.type));
		
		String repoName=(String)op.get("workspace");
		assertNull("Invalid workspace name: "+repoName, repoName);
		
		String username=(String)op.get("user");
		assertTrue("Invalid user name: "+username+", expected: admin", username.equals("admin"));
		
		System.out.println("Completed login test.\n");
	}
}
