package com.springsource.insight.plugin.struts2.test.action;

import com.opensymphony.xwork2.ActionSupport;
import com.springsource.insight.plugin.struts2.test.model.Person;

/**
 * Struts2 tests action without validation
 * Acts as a simple controller to handle actions related to registering a user.
 *
 */
public class RegisterAction extends ActionSupport {
	private static final long serialVersionUID = 1L;
	
	private Person personBean;
	
	
	public String process() throws Exception {
		
		//call Service class to store personBean's state in database
		personBean.setAge(100);
		personBean.setFirstName("John");
		personBean.setLastName("Dow");
		
		return SUCCESS;
		
	}

	
	public Person getPersonBean() {
		
		return personBean;
		
	}
	
	public void setPersonBean(Person person) {
		
		personBean = person;
		
	}

}
