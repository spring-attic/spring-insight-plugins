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

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;


public class ActionOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	@Test
	public void test1() {
		// Step 1: Execute test
		WebFlowExecutionTest webFlow=new WebFlowExecutionTest();
		webFlow.testAction();

		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();

		// Step 3:  Validate
		assertNotNull(op);
		assert op.getType().getName().equals("wf-action");

		assertNotNull(op.get("action"));
		assert "flowScope.person=personDao.findPersonById(id)".equals(op.get("action"));
	}

	@Override
	public OperationCollectionAspectSupport getAspect() {
		return ActionOperationCollectionAspect.aspectOf();
	}
}