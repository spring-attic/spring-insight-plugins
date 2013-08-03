package com.springsource.insight.plugin.jsf;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.idk.WebApplicationContextLoader;
import com.springsource.insight.intercept.operation.Operation;

/**
 * This test verifies that JSF action listeners are correctly captured by the
 * aspect, {@link ActionListenerOperationCollectionAspect}.
 */
@ContextConfiguration(locations = {
		"classpath:META-INF/insight-plugin-jsf.xml",
		"classpath:META-INF/test-app-context.xml" }, loader = WebApplicationContextLoader.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ActionListenerOperationCollectionAspectTest extends
		OperationCollectionAspectTestSupport {

	private ActionEvent actionEvent = mock(ActionEvent.class,
			RETURNS_DEEP_STUBS);

	@Test
	public void myOperationCollected() {
		/**
		 * First step: Execute whatever method is matched by our pointcut in
		 * {@link ActionListenerOperationCollectionAspect}
		 * 
		 */
		ContextMocker.mockFacesContext();
		MockActionListener bean = new MockActionListener();
		bean.processAction(actionEvent);

		/**
		 * Second step: Snatch the operation that was just created
		 */
		Operation op = getLastEntered();

		/**
		 * Third step: Validate that our operation has been created as we expect
		 */
		assertEquals(MockActionListener.class.getName(), op
				.getSourceCodeLocation().getClassName());
		assertEquals("processAction", op.getSourceCodeLocation()
				.getMethodName());
		assertEquals("No Action", op.get("fromAction"));
	}

	private static class MockActionListener implements ActionListener {

		public void processAction(ActionEvent event)
				throws AbortProcessingException {
		}
	}

	@Override
	public OperationCollectionAspectSupport getAspect() {
		return ActionListenerOperationCollectionAspect.aspectOf();
	}
}
