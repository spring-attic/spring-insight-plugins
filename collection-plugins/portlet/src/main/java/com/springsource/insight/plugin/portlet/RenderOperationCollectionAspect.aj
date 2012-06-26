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

package com.springsource.insight.plugin.portlet;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.RenderMode;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;

/**
 * This aspect create insight operation for Portlet render
 * @type: portlet-render
 */
public privileged aspect RenderOperationCollectionAspect extends GenericOperationCollectionAspect {
    public RenderOperationCollectionAspect () {
    	super();
    }

    public pointcut collectionPoint() : execution(void javax.portlet.Portlet+.render(RenderRequest, RenderResponse)) ||
									    execution(void javax.portlet.GenericPortlet+.doView(RenderRequest, RenderResponse)) ||
									    execution(void javax.portlet.GenericPortlet+.doEdit(RenderRequest, RenderResponse)) ||
    									execution(@RenderMode void *(RenderRequest, RenderResponse));

	@Override
	protected Operation createOperation(JoinPoint jp) {
		Object[] args = jp.getArgs();
		RenderRequest req=(RenderRequest)args[0];
		
		Operation op=createOperation(jp, OperationCollectionTypes.RENDER_TYPE);
		try {
			//portlet2 support
			op.put("renderPhase", req.getParameter(RenderRequest.RENDER_PHASE));
	        op.put("renderPart", req.getParameter(RenderRequest.RENDER_PART));
	        op.put("ETag", req.getETag());
		}
		catch(Error e) {
			// ignored
		}
		
		return op;
	}
}
