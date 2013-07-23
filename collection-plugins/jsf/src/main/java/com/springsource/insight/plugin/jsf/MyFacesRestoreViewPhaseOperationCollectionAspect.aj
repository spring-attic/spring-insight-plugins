package com.springsource.insight.plugin.jsf;

import javax.faces.context.FacesContext;

import org.apache.myfaces.lifecycle.DefaultRestoreViewSupport;
import org.apache.myfaces.lifecycle.RestoreViewSupport;

import com.springsource.insight.intercept.operation.OperationType;

public aspect MyFacesRestoreViewPhaseOperationCollectionAspect extends
		AbstractRestoreViewPhaseOperationCollectionAspect {

	static final OperationType TYPE = OperationType
			.valueOf("restore_view_phase_operation");

	public pointcut collectionPoint()
        : execution(boolean org.apache.myfaces.lifecycle.RestoreViewExecutor.execute(FacesContext));

	@Override
    protected String calculateViewId(FacesContext facesContext) {
        RestoreViewSupport restoreViewSupport = new DefaultRestoreViewSupport();
        return restoreViewSupport.calculateViewId(facesContext);
    }
}
