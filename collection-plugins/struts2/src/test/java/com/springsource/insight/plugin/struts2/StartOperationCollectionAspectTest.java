package com.springsource.insight.plugin.struts2;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;

/**
 * Test Collection operation for Struts2 flow execution start 
 *
 */
public class StartOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	
	@Test
	/**
	 * tests action without validation
	 */
	public void test1() throws Exception {
		// Step 1: Execute test
		Struts2Tests.getInstance().testExecutePasses();

		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();
		assertNotNull("No operation data is intercepted",op);

		// Step 3:  Validate
		assertTrue("Invalid operation type: "+op.getType().getName()+", expected: "+OperationCollectionTypes.START_TYPE.getValue(),
					op.getType().equals(OperationCollectionTypes.START_TYPE.type));

		assertTrue("Invalid action name: "+op.get("actionName")+", expected: /register2", "/register2".equals(op.get("actionName")));
		
		OperationMap params=(OperationMap) op.get("params");
		assertTrue("Invalid request parameters", params!=null && params.size()>0 && params.get("personBean.lastName")!=null);
	}
	
	@Test
	/**
	 * tests action with validation
	 */
	public void test2() throws Exception {
		// Step 1: Execute test
		Struts2Tests.getInstance().testExecuteValidationFailsMissingFirstName();

		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();
		assertNotNull("No operation data is intercepted",op);

		// Step 3:  Validate
		assertTrue("Invalid operation type: "+op.getType().getName()+", expected: "+OperationCollectionTypes.START_TYPE.getValue(),
					op.getType().equals(OperationCollectionTypes.START_TYPE.type));
		
		assertTrue("Invalid action name: "+op.get("actionName")+", expected: /register", "/register".equals(op.get("actionName")));
		
		OperationMap params=(OperationMap) op.get("params");
		assertTrue("Invalid result parameters", params!=null && params.size()>0 && params.get("personBean.lastName")!=null);
	}

	@Override
	public OperationCollectionAspectSupport getAspect() {
		return StartOperationCollectionAspect.aspectOf();
	}
}