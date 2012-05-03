package com.springsource.insight.plugin.struts2;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
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