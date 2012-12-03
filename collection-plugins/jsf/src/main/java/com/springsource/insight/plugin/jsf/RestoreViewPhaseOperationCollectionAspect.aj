/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
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
package com.springsource.insight.plugin.jsf;

import java.util.Map;

import javax.faces.context.FacesContext;

import com.springsource.insight.intercept.operation.OperationType;

public aspect RestoreViewPhaseOperationCollectionAspect extends AbstractRestoreViewPhaseOperationCollectionAspect {

	static final OperationType TYPE = OperationType
			.valueOf("restore_view_phase_operation");

	public pointcut collectionPoint()
        : execution (void com.sun.faces.lifecycle.RestoreViewPhase.execute(FacesContext));
	
    @SuppressWarnings("rawtypes")
	protected String calculateViewId(FacesContext facesContext) {
        Map requestMap = facesContext.getExternalContext().getRequestMap();
        String viewId = (String)
          requestMap.get("javax.servlet.include.path_info");
        if (viewId == null) {
            viewId = facesContext.getExternalContext().getRequestPathInfo();
        }

        // It could be that this request was mapped using
        // a prefix mapping in which case there would be no
        // path_info.  Query the servlet path.
        if (viewId == null) {
            viewId = (String)
              requestMap.get("javax.servlet.include.servlet_path");
        }

        if (viewId == null) {
            viewId = facesContext.getExternalContext().getRequestServletPath();
        }
        
        if (viewId == null) {
            viewId = "Unknown View";
        }
        
        return viewId;
	}
}
