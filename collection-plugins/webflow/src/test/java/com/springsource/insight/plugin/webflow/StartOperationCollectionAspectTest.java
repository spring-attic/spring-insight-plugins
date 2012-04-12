package com.springsource.insight.plugin.webflow;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.plugin.webflow.StartOperationCollectionAspect;


public class StartOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	@Test
	public void test1() {
		// Step 1: Execute test
		WebFlowExecutionTest webFlow=new WebFlowExecutionTest();
		webFlow.testFullFlow();

		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();

		// Step 3:  Validate
		assertNotNull(op);
		assert op.getType().getName().equals("wf-start");

		assertNotNull(op.get("flowId"));
		assertNotNull(op.get("initParams"));

		OperationMap map=(OperationMap) op.get("initParams");
		assertNotNull(map.get("id"));
	}

	@Override
	public OperationCollectionAspectSupport getAspect() {
		return StartOperationCollectionAspect.aspectOf();
	}
}