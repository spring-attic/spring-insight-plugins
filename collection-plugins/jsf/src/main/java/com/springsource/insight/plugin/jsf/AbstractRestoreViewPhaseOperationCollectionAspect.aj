package com.springsource.insight.plugin.jsf;

import javax.faces.context.FacesContext;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

public abstract aspect AbstractRestoreViewPhaseOperationCollectionAspect extends MethodOperationCollectionAspect {

	static final OperationType TYPE = OperationType
			.valueOf("restore_view_phase_operation");

	@Override
	protected Operation createOperation(JoinPoint jp) {
		FacesContext facesContext = (FacesContext) jp.getArgs()[0];
		String viewId = calculateViewId(facesContext);

		StringBuilder label = new StringBuilder("JSF Restore View Phase [");
		label.append(viewId);
		label.append("]");
		return super.createOperation(jp).type(TYPE).label(label.toString())
				.put("viewId", viewId)
				.put("isPostBack", facesContext.isPostback());
	}
	
	protected abstract String calculateViewId(FacesContext facesContext);
    @Override
    public String getPluginName() {
    	return JsfPluginRuntimeDescriptor.PLUGIN_NAME;
    }
	    
}
