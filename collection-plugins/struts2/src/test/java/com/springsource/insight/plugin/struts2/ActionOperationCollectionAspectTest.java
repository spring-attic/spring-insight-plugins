package com.springsource.insight.plugin.struts2;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

/**
 * Tests Collection operation for Struts2 action invocations 
 *
 */
public class ActionOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	
	@Test
	/**
	 * tests action without validation
	 **/
	public void test1() throws Exception {
		// Step 1: Execute test
		Struts2Tests.getInstance().testExecutePasses();
	
		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();
		assertNotNull("No operation data is intercepted",op);

		// Step 3:  Validate
		assertTrue("Invalid operation type: "+op.getType().getName()+", expected: "+OperationCollectionTypes.ACTION_TYPE.getValue(),
					op.getType().equals(OperationCollectionTypes.ACTION_TYPE.type));
		
		String action=(String)op.get("action");
		assertTrue("Invalid operation action: "+action+", expected: RegisterAction.process()",
					action!=null && action.endsWith("RegisterAction.process()"));
	}
	
	@Test
	/*
	 * tests action with validation
	 */
	public void test2() throws Exception {
		// Step 1: Execute test
		Struts2Tests.getInstance().testExecuteValidationFailsMissingFirstName();

		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();
		assertNotNull("No operation data is intercepted",op);

		// Step 3:  Validate
		assertTrue("Invalid operation type: "+op.getType().getName()+", expected: "+OperationCollectionTypes.ACTION_TYPE.getValue(),
					op.getType().equals(OperationCollectionTypes.ACTION_TYPE.type));

		String action=(String)op.get("action");
		assertTrue("Invalid operation action: "+action+", expected: RegisterValidationAction.validate()",
					action!=null && action.endsWith("RegisterValidationAction.validate()"));
	}

	@Override
	public OperationCollectionAspectSupport getAspect() {
		return ActionOperationCollectionAspect.aspectOf();
	}
}