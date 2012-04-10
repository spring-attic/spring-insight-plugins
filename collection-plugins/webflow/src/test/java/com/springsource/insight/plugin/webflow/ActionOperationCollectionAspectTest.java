package com.springsource.insight.plugin.webflow;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.plugin.webflow.ActionOperationCollectionAspect;


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