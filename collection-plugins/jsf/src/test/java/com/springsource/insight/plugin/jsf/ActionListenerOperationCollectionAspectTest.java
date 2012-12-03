/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.plugin.jsf;

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
import org.powermock.modules.junit4.PowerMockRunner;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

/**
 * This test verifies that JSF action listeners are correctly captured by the aspect,
 * {@link ActionListenerOperationCollectionAspect}.
 */
@SuppressWarnings("deprecation")
@RunWith(PowerMockRunner.class)
@PrepareForTest(FacesContext.class)
public class ActionListenerOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	
	private FacesContext mockFacesContext = mock(FacesContext.class, RETURNS_DEEP_STUBS);
	private static MethodExpression mockMethodExpression = mock(MethodExpression.class, RETURNS_DEEP_STUBS);
	private ActionEvent actionEvent = mock(ActionEvent.class, RETURNS_DEEP_STUBS);
	private MethodInfo mockMethodInfo = mock(MethodInfo.class);
	private ELContext mockElContext = mock(ELContext.class);

	@Test
	@Ignore
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
			return null;
		}

		public void restoreState(FacesContext context, Object state) {
		}

		public boolean isTransient() {
			return false;
		}

		public void setTransient(boolean newTransientValue) {
		}

		@Override
		public Map<String, Object> getAttributes() {
			return null;
		}

		@Override
		public ValueBinding getValueBinding(String name) {
			return null;
		}

		@Override
		public void setValueBinding(String name, ValueBinding binding) {
		}

		@Override
		public String getClientId(FacesContext context) {
			return null;
		}

		@Override
		public String getFamily() {
			return null;
		}

		@Override
		public String getId() {
			return null;
		}

		@Override
		public void setId(String id) {
		}

		@Override
		public UIComponent getParent() {
			return null;
		}

		@Override
		public void setParent(UIComponent parent) {
		}

		@Override
		public boolean isRendered() {
			return false;
		}

		@Override
		public void setRendered(boolean rendered) {
		}

		@Override
		public String getRendererType() {
			return null;
		}

		@Override
		public void setRendererType(String rendererType) {
		}

		@Override
		public boolean getRendersChildren() {
			return false;
		}

		@Override
		public List<UIComponent> getChildren() {
			return null;
		}

		@Override
		public int getChildCount() {
			return 0;
		}

		@Override
		public UIComponent findComponent(String expr) {
			return null;
		}

		@Override
		public Map<String, UIComponent> getFacets() {
			return null;
		}

		@Override
		public UIComponent getFacet(String name) {
			return null;
		}

		@Override
		public Iterator<UIComponent> getFacetsAndChildren() {
			return null;
		}

		@Override
		public void broadcast(FacesEvent event) throws AbortProcessingException {
		}

		@Override
		public void decode(FacesContext context) {
		}

		@Override
		public void encodeBegin(FacesContext context) throws IOException {
		}

		@Override
		public void encodeChildren(FacesContext context) throws IOException {
		}

		@Override
		public void encodeEnd(FacesContext context) throws IOException {
		}

		@Override
		protected void addFacesListener(FacesListener listener) {
		}

		@Override
		protected FacesListener[] getFacesListeners(Class clazz) {
			return null;
		}

		@Override
		protected void removeFacesListener(FacesListener listener) {
		}

		@Override
		public void queueEvent(FacesEvent event) {
		}

		@Override
		public void processRestoreState(FacesContext context, Object state) {
		}

		@Override
		public void processDecodes(FacesContext context) {
		}

		@Override
		public void processValidators(FacesContext context) {
		}

		@Override
		public void processUpdates(FacesContext context) {
		}

		@Override
		public Object processSaveState(FacesContext context) {
			return null;
		}

		@Override
		protected FacesContext getFacesContext() {
			return null;
		}

		@Override
		protected Renderer getRenderer(FacesContext context) {
			return null;
		}

		public MethodBinding getAction() {
			return null;
		}

		public void setAction(MethodBinding action) {
		}

		public MethodBinding getActionListener() {
			return null;
		}

		public void setActionListener(MethodBinding actionListener) {
		}

		public boolean isImmediate() {
			return false;
		}

		public void setImmediate(boolean immediate) {
		}

		public void addActionListener(ActionListener listener) {
		}

		public ActionListener[] getActionListeners() {
			return null;
		}

		public void removeActionListener(ActionListener listener) {
		}

		public MethodExpression getActionExpression() {
			return mockMethodExpression;
		}

		public void setActionExpression(MethodExpression action) {
		}
		
	}

	@Override
	public OperationCollectionAspectSupport getAspect() {
		return ActionListenerOperationCollectionAspect.aspectOf();
	}
}
