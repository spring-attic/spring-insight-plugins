package com.springsource.insight.plugin.jsf;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


public abstract class ContextMocker extends FacesContext {
	private ContextMocker() {
	}

	private static final Release RELEASE = new Release();
	private static final GetELContext GET_EL_CONTEXT = new GetELContext();
	private static final GetApplication GET_APPLICATION = new GetApplication();

	private static class Release implements Answer<Void> {
		public Void answer(InvocationOnMock invocation) throws Throwable {
			setCurrentInstance(null);
			return null;
		}
	}

	private static class GetELContext implements Answer<ELContext> {
		public ELContext answer(InvocationOnMock invocation) throws Throwable {
			ELContext elContext = mock(ELContext.class);
			return elContext;
		}
	}
		
	private static class GetApplication implements Answer<Application> {
		public Application answer(InvocationOnMock invocation) throws Throwable {
			Application application = mock(Application.class);
			
			ExpressionFactory mockExpressionFactory = mock(ExpressionFactory.class); 
			when(application.getExpressionFactory()).thenReturn(mockExpressionFactory);
			
			ValueExpression mockValueExpression = mock(ValueExpression.class);
			when(mockExpressionFactory.createValueExpression(any(ELContext.class), anyString(), any(Class.class))).thenReturn(mockValueExpression);
			when(mockExpressionFactory.createValueExpression(any(Object.class), any(Class.class))).thenReturn(mockValueExpression);
			
			when(mockValueExpression.getValue(any(ELContext.class))).thenReturn(null);
			
			return application;
		}
	}

	public static FacesContext mockFacesContext() {
		FacesContext context = mock(FacesContext.class);
		setCurrentInstance(context);

		doAnswer(RELEASE).when(context).release();
		doAnswer(GET_EL_CONTEXT).when(context).getELContext();
		doAnswer(GET_APPLICATION).when(context).getApplication();
		
		return context;
	}
}
