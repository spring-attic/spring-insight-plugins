package com.springsource.insight.plugin.webflow;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.plugin.webflow.StateOperationCollectionAspect;


public class StateOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	@Test
	public void test1() {
		// Step 1: Execute test
		WebFlowExecutionTest webFlow=new WebFlowExecutionTest();
		webFlow.testState();

		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();

		// Step 3:  Validate
		assertNotNull(op);
		assert op.getType().getName().equals("wf-state");

		assert "ViewState".equals(op.get("stateType"));
		assert "dummy2".equals(op.get("stateId"));
		//assert "/dummyView".equals(op.get("view"));
		OperationMap map=(OperationMap)op.get("attribs");
		assertNotNull(map.get("model"));

		OperationList entryActions=(OperationList)op.get("entryActions");
		assert "personDao.save(person)".equals(entryActions.get(0));
		assert "flowScope.temp=1".equals(entryActions.get(1));
	}

	@Override
	public OperationCollectionAspectSupport getAspect() {
		return StateOperationCollectionAspect.aspectOf();
	}
}