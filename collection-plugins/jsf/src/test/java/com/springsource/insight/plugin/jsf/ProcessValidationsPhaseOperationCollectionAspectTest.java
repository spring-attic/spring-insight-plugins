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
package com.springsource.insight.plugin.jsf;

import javax.faces.context.FacesContext;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.sun.faces.lifecycle.ProcessValidationsPhase;

/**
 * This test verifies that JSF validators are correctly captured by the aspect,
 * {@link ProcessValidationsPhaseOperationCollectionAspect}.
 */
public class ProcessValidationsPhaseOperationCollectionAspectTest extends
		OperationCollectionAspectTestSupport {

	@Test
	public void myOperationCollected() {
		/**
		 * First step: Execute whatever method is matched by our pointcut in
		 * {@link ProcessValidationsPhaseOperationCollectionAspect}
		 * 
		 */
		MockPhase bean = new MockPhase();
		bean.execute(null);

		/**
		 * Second step: Snatch the operation that was just created
		 */
		Operation op = getLastEntered();

		/**
		 * Third step: Validate that our operation has been created as we expect
		 */
		assertEquals(MockPhase.class.getName(), op.getSourceCodeLocation()
				.getClassName());
		assertEquals("execute", op.getSourceCodeLocation().getMethodName());
	}

	private static class MockPhase extends ProcessValidationsPhase {

		@Override
		public void execute(FacesContext facesContext) {
		}

	}

	@Override
	public OperationCollectionAspectSupport getAspect() {
		return ProcessValidationsPhaseOperationCollectionAspect.aspectOf();
	}
}
