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

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

/**
 * Tests Collection operation for Struts2 custom interceptors
 */
public class InterceptOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	
	//@Test
	public void test1() throws Exception {
		// Step 1: Execute test
		Struts2Tests.getInstance().testExecutePasses();

		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();
		assertNotNull("No operation data is intercepted",op);

		// Step 3:  Validate
		assertTrue("Invalid operation type: "+op.getType().getName()+", expected: "+OperationCollectionTypes.INTERCEPT_TYPE.getValue(),
					op.getType().equals(OperationCollectionTypes.INTERCEPT_TYPE.type));

		String interceptor=(String)op.get("interceptor");
		assertTrue("Invalid interceptor class",interceptor.endsWith("MyInterceptor"));
	}

	@Override
	public OperationCollectionAspectSupport getAspect() {
		return InterceptOperationCollectionAspect.aspectOf();
	}
}
