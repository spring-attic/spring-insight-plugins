package com.springsource.insight.plugin.jsf;

import javax.faces.context.FacesContext;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

public aspect RenderResponsePhaseOperationCollectionAspect extends
		MethodOperationCollectionAspect {

	static final OperationType TYPE = OperationType
			.valueOf("render_response_phase_operation");

	public pointcut collectionPoint()
        : execution(boolean org.apache.myfaces.lifecycle.RenderResponseExecutor.execute(FacesContext))
        	|| execution (void com.sun.faces.lifecycle.RenderResponsePhase.execute(FacesContext));

	@Override
	protected Operation createOperation(JoinPoint jp) {
		FacesContext facesContext = (FacesContext) jp.getArgs()[0];

		String viewId = facesContext.getViewRoot().getViewId();
		return super.createOperation(jp).type(TYPE)
				.label("JSF Render Response Phase [" + viewId + "]")
				.put("viewId", viewId);
	}
    @Override
    public String getPluginName() {
     	return JsfPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
