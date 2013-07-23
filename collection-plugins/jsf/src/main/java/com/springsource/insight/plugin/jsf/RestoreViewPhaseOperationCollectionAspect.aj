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
