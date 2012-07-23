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

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;


/**
 * This aspect intercepts all JCR GetNode(s) requests
 */
public privileged aspect GetNodeOperationCollectionAspect extends AbstractOperationCollectionAspect {
    
	public pointcut getNode(): execution(public Node javax.jcr.Node+.getNode(String)) && if(GetNodeOperationCollectionAspect.isGetItemEnabled(thisJoinPoint));;
	
	public pointcut getNodes(): execution(public NodeIterator javax.jcr.Node+.getNodes(..));
	
	public pointcut getItem(): execution(public Item javax.jcr.Session+.getItem(String));
	
	
	public pointcut collectionPoint() : getNode() || getNodes() || getItem();
	
	
	public static boolean isGetItemEnabled(JoinPoint jp) {
    	Item item=(Item)jp.getTarget();
    	try {
			return !item.getSession().getUserID().equals("system"); // filter get requests in chain
		}
		catch (RepositoryException e) {
			return false;
		}
    }

    @Override
    protected Operation createOperation(JoinPoint jp) {
    	String method=jp.getSignature().getName(); //method name
    	Object targ=jp.getTarget();
    			
    	Operation op=new Operation().type(OperationCollectionTypes.GET_TYPE.type)
    								.label(OperationCollectionTypes.GET_TYPE.label+method)
    								.sourceCodeLocation(getSourceCodeLocation(jp))
    								.put("workspace", JCRCollectionUtils.getWorkspaceName(targ));

    	try {
			if (targ instanceof Item) {
				op.put("path", ((Item)targ).getPath()); //relating node path
			}
		}
		catch (RepositoryException e) {
			//ignore
		}

    	//add request parameters
    	Object[] args = jp.getArgs();
    	
    	if (method.equals("getNode"))
    		op.put("relPath", (String)args[0]);
    	else
    	if (method.equals("getNodes") && args.length>0)
    		op.put("namePattern", (String)args[0]);
    	else
        if (method.equals("getItem"))
        	op.put("absPath", (String)args[0]);
    			
    	return op;
    }

	@Override
	public String getPluginName() {
		return JCRPluginRuntimeDescriptor.PLUGIN_NAME;
	}
}