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

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.ArrayUtil;

/**
 * This aspect intercepts all JCR Workspace/Session requests for: save, refresh, copy, clone, move
 */
public privileged aspect WorkspaceOperationCollectionAspect extends AbstractOperationCollectionAspect {
	public WorkspaceOperationCollectionAspect () {
		super();
	}

	public pointcut workspaceCopy(): execution(public void javax.jcr.Workspace+.copy(String, String)) ||
									execution(public void javax.jcr.Workspace+.copy(String, String, String));
	
	public pointcut workspaceClone(): execution(public void javax.jcr.Workspace+.clone(String, String, String, boolean));
	
	public pointcut workspaceMove(): execution(public void javax.jcr.Workspace+.move(String, String)) ||
									execution(public void javax.jcr.Session+.move(String, String));
	
    public pointcut collectionPoint() : workspaceCopy() || workspaceClone() || workspaceMove();

    @Override
    protected Operation createOperation(JoinPoint jp) {
    	String method=jp.getSignature().getName();
    	Object[] args = jp.getArgs();
    			
    	Operation op=new Operation().type(OperationCollectionTypes.WORKSPACE_TYPE.type)
    								.label(OperationCollectionTypes.WORKSPACE_TYPE.label+" "+method)
    								.sourceCodeLocation(getSourceCodeLocation(jp))
    								.putAnyNonEmpty("workspace", JCRCollectionUtils.getWorkspaceName(jp.getTarget()))
    								;
    	
    	//add request parameters
    	if (method.equals("copy")) {
    		createCopyOperation(op, args);
    	} else if (method.equals("clone")) {
    		createCloneOperation(op, args);
    	} else if (method.equals("move")) {
    		createMoveOperation(op, args);
    	}

    	return op;
    }
    
    static Operation createCopyOperation(Operation op, Object ... args) {
    	if (ArrayUtil.length(args) <= 1) {
    		return op;
    	}

    	op.putAnyNonEmpty("destAbsPath", args[args.length-1])
    	  .putAnyNonEmpty("srcAbsPath", args[args.length-2])
    	  ;
    	if (args.length==3) {
    		op.putAnyNonEmpty("srcWorkspace", args[0]);
    	}

    	return op;
    }
    
    static Operation createCloneOperation(Operation op, Object ... args) {
    	if (ArrayUtil.length(args) <= 3) {
    		return op;
    	}

    	return op.putAnyNonEmpty("srcWorkspace", args[0])
    			 .putAnyNonEmpty("srcAbsPath", args[1])
    			 .putAnyNonEmpty("destAbsPath", args[2])
    			 .putAnyNonEmpty("removeExisting", args[3])
    			 ;
    }
    
    static Operation createMoveOperation(Operation op, Object ... args) {
    	if (ArrayUtil.length(args) <= 1) {
    		return op;
    	}

    	return op.putAnyNonEmpty("srcAbsPath", args[0])
    			 .putAnyNonEmpty("destAbsPath", args[1])
    			 ;
    }

	@Override
	public String getPluginName() {
		return JCRPluginRuntimeDescriptor.PLUGIN_NAME;
	}
}