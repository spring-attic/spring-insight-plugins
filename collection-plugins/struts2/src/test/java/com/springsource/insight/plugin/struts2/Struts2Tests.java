package com.springsource.insight.plugin.struts2;

import org.apache.struts2.StrutsTestCase;

import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.ActionSupport;
import com.springsource.insight.plugin.struts2.test.action.RegisterAction;
import com.springsource.insight.plugin.struts2.test.action.RegisterValidationAction;

/**
 * Struts2 tests set
 *
 */
public class Struts2Tests extends StrutsTestCase {
	private static Struts2Tests instance=new Struts2Tests();
	
	public static Struts2Tests getInstance()  {
		return instance;
	}
	
	private Struts2Tests() {
		try {
			super.setUp();
		}
		catch (Exception e) {
			assertNotNull("The Struts2 context is not initiated: "+e, null);
		}
	}
	
	/**
	 * tests Struts2 action flow without validation
	 * @throws Exception
	 */
	public void testExecutePasses() throws Exception {
		super.initServletMockObjects();
		assertNotNull("The servlet context is not initiated", request);
	
		request.setParameter("personBean.firstName", "Bruce");
		request.setParameter("personBean.lastName", "Phillips");
		request.setParameter("personBean.email", "bphillips@ku.edu");
		request.setParameter("personBean.age", "19");
		ActionProxy actionProxy = getActionProxy("/register2.action");
		RegisterAction action = (RegisterAction) actionProxy.getAction();
		assertNotNull("The action is null but should not be.", action);
		
		String result = actionProxy.execute();
		assertEquals("The execute method did not return " + ActionSupport.SUCCESS + " but should have.", ActionSupport.SUCCESS, result);
	}

	/**
	 * tests Struts2 action flow with validation
	 * @throws Exception
	 */
	public void testExecuteValidationFailsMissingFirstName() throws Exception {
		super.initServletMockObjects();
		assertNotNull("The servlet context is not initiated", request);
	
		request.setParameter("personBean.lastName", "Phillips");
		request.setParameter("personBean.email", "bphillips@ku.edu");
		request.setParameter("personBean.age", "19");
		ActionProxy actionProxy = getActionProxy("/register.action");
		RegisterValidationAction action = (RegisterValidationAction) actionProxy.getAction();
		assertNotNull("The action is null but should not be.", action);
	
		String result = actionProxy.execute();
		assertEquals("The execute method did not return " + ActionSupport.INPUT	+ " but should have.", ActionSupport.INPUT, result);

	}
}
