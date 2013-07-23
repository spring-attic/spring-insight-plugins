package com.springsource.insight.plugin.jsf;

import javax.faces.context.FacesContext;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

public aspect InvokeApplicationPhaseOperationCollectionAspect extends
		MethodOperationCollectionAspect {

	public static final OperationType TYPE = OperationType
			.valueOf("invoke_application_phase_operation");

	public pointcut collectionPoint()
        : execution(boolean org.apache.myfaces.lifecycle.InvokeApplicationExecutor.execute(FacesContext))
        	|| execution (void com.sun.faces.lifecycle.InvokeApplicationPhase.execute(FacesContext));

	@Override
	protected Operation createOperation(JoinPoint jp) {
		return super.createOperation(jp).type(TYPE)
				.label("JSF Invoke Application Phase");
	}
    @Override
    public String getPluginName() {
       	return JsfPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
