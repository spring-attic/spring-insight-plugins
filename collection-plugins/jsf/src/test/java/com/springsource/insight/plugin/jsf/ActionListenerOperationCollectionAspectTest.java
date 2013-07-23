package com.springsource.insight.plugin.jsf;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.MethodInfo;
import javax.faces.component.ActionSource2;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import javax.faces.render.Renderer;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

/**
 * This test verifies that JSF action listeners are correctly captured by the aspect,
 * {@link ActionListenerOperationCollectionAspect}.
 */
//@PowerMockIgnore("com.springsource.insight.*")
//@SuppressWarnings("deprecation")
//@RunWith(PowerMockRunner.class)
//@PrepareForTest(FacesContext.class)
public class ActionListenerOperationCollectionAspectTest extends
		OperationCollectionAspectTestSupport {
	
	private FacesContext mockFacesContext = mock(FacesContext.class, RETURNS_DEEP_STUBS);
	private static MethodExpression mockMethodExpression = mock(MethodExpression.class, RETURNS_DEEP_STUBS);
	private ActionEvent actionEvent = mock(ActionEvent.class, RETURNS_DEEP_STUBS);
	private MethodInfo mockMethodInfo = mock(MethodInfo.class);
	private ELContext mockElContext = mock(ELContext.class);

	//@Test
	//@Ignore
	public void myOperationCollected() {
		/**
		 * First step: Execute whatever method is matched by our pointcut in
		 * {@link ActionListenerOperationCollectionAspect}
		 * 
		 */
		PowerMockito.mockStatic(FacesContext.class);
		
		when(FacesContext.getCurrentInstance()).thenReturn(mockFacesContext);
		when(mockFacesContext.getELContext()).thenReturn(mockElContext);
		when(actionEvent.getComponent()).thenReturn(new MockUIComponent());
		when(mockMethodExpression.isLiteralText()).thenReturn(false);
		when(mockMethodExpression.getExpressionString()).thenReturn("fromAction");
		when(mockMethodExpression.getMethodInfo(mockElContext)).thenReturn(mockMethodInfo);
		
		MockActionListener bean = new MockActionListener();
		bean.processAction(actionEvent);

		/**
		 * Second step: Snatch the operation that was just created
		 */
		Operation op = getLastEntered();

		/**
		 * Third step: Validate that our operation has been created as we expect
		 */
		assertEquals(MockActionListener.class.getName(), op.getSourceCodeLocation()
				.getClassName());
		assertEquals("processAction", op.getSourceCodeLocation().getMethodName());
		assertEquals("fromAction", op.get("fromAction"));
	}

	private static class MockActionListener implements ActionListener {

		public void processAction(ActionEvent event)
				throws AbortProcessingException {	
		}
	}
	
	private static class MockUIComponent extends UIComponent implements ActionSource2 {

		public Object saveState(FacesContext context) {
			// TODO Auto-generated method stub
			return null;
		}

		public void restoreState(FacesContext context, Object state) {
			// TODO Auto-generated method stub
			
		}

		public boolean isTransient() {
			// TODO Auto-generated method stub
			return false;
		}

		public void setTransient(boolean newTransientValue) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Map<String, Object> getAttributes() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ValueBinding getValueBinding(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setValueBinding(String name, ValueBinding binding) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getClientId(FacesContext context) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getFamily() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setId(String id) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public UIComponent getParent() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setParent(UIComponent parent) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isRendered() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setRendered(boolean rendered) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getRendererType() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setRendererType(String rendererType) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean getRendersChildren() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public List<UIComponent> getChildren() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getChildCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public UIComponent findComponent(String expr) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map<String, UIComponent> getFacets() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public UIComponent getFacet(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Iterator<UIComponent> getFacetsAndChildren() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void broadcast(FacesEvent event) throws AbortProcessingException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void decode(FacesContext context) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void encodeBegin(FacesContext context) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void encodeChildren(FacesContext context) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void encodeEnd(FacesContext context) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void addFacesListener(FacesListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected FacesListener[] getFacesListeners(Class clazz) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected void removeFacesListener(FacesListener listener) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void queueEvent(FacesEvent event) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void processRestoreState(FacesContext context, Object state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void processDecodes(FacesContext context) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void processValidators(FacesContext context) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void processUpdates(FacesContext context) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Object processSaveState(FacesContext context) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected FacesContext getFacesContext() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected Renderer getRenderer(FacesContext context) {
			// TODO Auto-generated method stub
			return null;
		}

		public MethodBinding getAction() {
			// TODO Auto-generated method stub
			return null;
		}

		public void setAction(MethodBinding action) {
			// TODO Auto-generated method stub
			
		}

		public MethodBinding getActionListener() {
			// TODO Auto-generated method stub
			return null;
		}

		public void setActionListener(MethodBinding actionListener) {
			// TODO Auto-generated method stub
			
		}

		public boolean isImmediate() {
			// TODO Auto-generated method stub
			return false;
		}

		public void setImmediate(boolean immediate) {
			// TODO Auto-generated method stub
			
		}

		public void addActionListener(ActionListener listener) {
			// TODO Auto-generated method stub
			
		}

		public ActionListener[] getActionListeners() {
			// TODO Auto-generated method stub
			return null;
		}

		public void removeActionListener(ActionListener listener) {
			// TODO Auto-generated method stub
			
		}

		public MethodExpression getActionExpression() {
			return mockMethodExpression;
		}

		public void setActionExpression(MethodExpression action) {
			// TODO Auto-generated method stub
			
		}
		
	}

	@Override
	public OperationCollectionAspectSupport getAspect() {
		return ActionListenerOperationCollectionAspect.aspectOf();
	}
}
