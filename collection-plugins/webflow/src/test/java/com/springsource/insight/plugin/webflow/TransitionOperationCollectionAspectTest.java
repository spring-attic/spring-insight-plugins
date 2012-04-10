package com.springsource.insight.plugin.webflow;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.plugin.webflow.TransitionOperationCollectionAspect;


public class TransitionOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	@Test
	public void test1() {
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