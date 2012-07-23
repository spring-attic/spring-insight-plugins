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

package com.springsource.insight.plugin.jcr;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;

/**
 * This aspect intercepts all JCR Login
 */
public privileged aspect LoginOperationCollectionAspect extends AbstractOperationCollectionAspect {

	public pointcut collectionPoint() : execution(public Session javax.jcr.Repository+.login(Credentials, String)) && if(LoginOperationCollectionAspect.isEnabled(thisJoinPoint));
    
	
    public static boolean isEnabled(JoinPoint jp) {
    	Repository repo=(Repository)jp.getTarget();
    	return repo.getClass().getSimpleName().endsWith("Impl"); // filter multiple login requests in chain
    }

    @Override
    protected Operation createOperation(JoinPoint jp) {
    	Object[] args = jp.getArgs();
    	Repository repo=(Repository)jp.getTarget();
    	
    	SimpleCredentials credentials=(SimpleCredentials)args[0];
    	String workspaceName=(String)args[1];
    			
    	Operation op=new Operation().type(OperationCollectionTypes.LOGIN_TYPE.type)
    								.label(OperationCollectionTypes.LOGIN_TYPE.label+(workspaceName!=null?" ["+workspaceName+"]":""))
    								.sourceCodeLocation(getSourceCodeLocation(jp))
    								.put("repository", repo.getDescriptor(Repository.REP_NAME_DESC))
    								.put("workspace", workspaceName);
    						
    	if (credentials!=null) {
    		op.put("user", credentials.getUserID());
    		op.put("pass", credentials.getPassword().toString());
    	}
    	
    	return op;
    }

	@Override
	public String getPluginName() {
		return JCRPluginRuntimeDescriptor.PLUGIN_NAME;
	}
}