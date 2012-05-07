/**
 * Copyright 2009-2011 the original author or authors.
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
