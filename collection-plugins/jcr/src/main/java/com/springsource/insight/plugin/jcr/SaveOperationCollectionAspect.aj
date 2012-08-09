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
import javax.jcr.RepositoryException;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;


/**
 * This aspect intercepts all JCR Item requests for: save, refresh, remove, update and addNode
 */
public privileged aspect SaveOperationCollectionAspect extends AbstractOperationCollectionAspect {
    public SaveOperationCollectionAspect () {
    	super();
    }

    public pointcut itemSave(): execution(public void javax.jcr.Item+.save()) && if(SaveOperationCollectionAspect.isItemSaveEnabled(thisJoinPoint));
    public pointcut itemRefresh(): execution(public void javax.jcr.Item+.refresh(boolean));
	
	public pointcut collectionPoint() : itemSave() || itemRefresh();
	
	public static boolean isItemSaveEnabled(JoinPoint jp) {
    	Item item=(Item)jp.getTarget();
    	try {
			return !item.getSession().getUserID().equals("system"); // filter save requests in chain
		} catch (RepositoryException e) {
			return false;
		}
    }

    @Override
    protected Operation createOperation(JoinPoint jp) {
    	Item item=(Item)jp.getTarget();
    	String path=null;
    	try {
    		path=item.getPath(); //relating item path
		} catch (RepositoryException e) {
			//ignore
		}

    	String method=jp.getSignature().getName();
    	Operation op=new Operation().type(OperationCollectionTypes.WORKSPACE_TYPE.type)
    								.label(OperationCollectionTypes.WORKSPACE_TYPE.label+" "+method+" ["+path+"]")
    								.sourceCodeLocation(getSourceCodeLocation(jp))
    								.putAnyNonEmpty("workspace", JCRCollectionUtils.getWorkspaceName(item))
    								.putAnyNonEmpty("path", path);
    	
    	//add request parameters
    	Object[] args = jp.getArgs();
    	if (method.equals("refresh")) {
    		op.put("keepChanges", ((Boolean)args[0]).booleanValue());
    	}

    	return op;
    }

	@Override
	public String getPluginName() {
		return JCRPluginRuntimeDescriptor.PLUGIN_NAME;
	}
}