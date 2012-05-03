package com.springsource.insight.plugin.struts2;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

import org.apache.struts2.convention.annotation.Action;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;

/**
 * Collection operation for Struts2 action invocations 
 */
public privileged aspect ActionOperationCollectionAspect extends AbstractOperationCollectionAspect {
	public pointcut collectionPoint() :
		( ( // execution action
			execution(public String com.opensymphony.xwork2.ActionSupport+.*())
			||
			// validate action
			execution(public void com.opensymphony.xwork2.ActionSupport+.validate())
		  )
		  // do not collect Struts2 internal actions
		  && !within(com.opensymphony.xwork2.ActionSupport) 
		)
		// action defined by annotation
		|| execution(@Action public String *()); 
		

	protected Operation createOperation(JoinPoint jp) {
		Signature actionSign = jp.getSignature();
		String action = actionSign.getDeclaringTypeName() + "." + actionSign.getName() + "()";

		return new Operation().type(OperationCollectionTypes.ACTION_TYPE.type)
				.label(OperationCollectionTypes.ACTION_TYPE.label + " [" + actionSign.toShortString() + "]")
				.sourceCodeLocation(getSourceCodeLocation(jp))
				.put("action", action); // action signature: Class.method()
	}

	@Override
	public String getPluginName() {
		return "struts2";
	}
}
