package com.springsource.insight.plugin.jsf;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

/**
 * This test verifies that JSF validators are correctly captured by the aspect,
 * {@link ValidatorOperationCollectionAspect}.
 */
public class ValidatorOperationCollectionAspectTest extends
		OperationCollectionAspectTestSupport {

	@Test
	public void myOperationCollected() {
		/**
		 * First step: Execute whatever method is matched by our pointcut in
		 * {@link ValidatorOperationCollectionAspect}
		 * 
		 */
		UIComponent mockUiComponent = mock(UIComponent.class);
		when(mockUiComponent.getId()).thenReturn("component id");

		Object mockObject = mock(Object.class);
		when(mockObject.toString()).thenReturn("test");

		MockValidator bean = new MockValidator();
		bean.validate(null, mockUiComponent, mockObject);

		/**
		 * Second step: Snatch the operation that was just created
		 */
		Operation op = getLastEntered();

		/**
		 * Third step: Validate that our operation has been created as we expect
		 */
		assertEquals(MockValidator.class.getName(), op.getSourceCodeLocation()
				.getClassName());
		assertEquals("validate", op.getSourceCodeLocation().getMethodName());
		assertEquals("component id", op.get("uiComponentId"));
		assertEquals("test", op.get("value"));
	}

	private static class MockValidator implements Validator {

		public void validate(FacesContext context, UIComponent component,
				Object value) throws ValidatorException {
		}
	}

	@Override
	public OperationCollectionAspectSupport getAspect() {
		return ValidatorOperationCollectionAspect.aspectOf();
	}
}
