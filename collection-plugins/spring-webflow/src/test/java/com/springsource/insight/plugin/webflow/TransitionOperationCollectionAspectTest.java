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

package com.springsource.insight.plugin.webflow;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;


public class TransitionOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	public TransitionOperationCollectionAspectTest () {
		super();
	}

	@Test
	public void testTransition() {
		// Step 1: Execute test
		WebFlowExecutionTest webFlow=new WebFlowExecutionTest();
		webFlow.testTransition();

		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();

		// Step 3:  Validate
		assertNotNull(op);
		assert op.getType().getName().equals("wf-transition");

		assert "cancel".equals(op.get("codeId"));
		assert "cancel".equals(op.get("stateId"));

		OperationMap map=(OperationMap) op.get("attribs");
		assertNotNull(map.get("bind"));

		OperationList list=(OperationList) op.get("actions");
		assert "flowScope.persons=personDao.findPersons()".equals(list.get(0));
	}

	@Override
	public OperationCollectionAspectSupport getAspect() {
		return TransitionOperationCollectionAspect.aspectOf();
	}
}