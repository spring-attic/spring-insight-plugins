package com.springsource.insight.plugin.jsf;

import javax.faces.context.FacesContext;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

public aspect ProcessValidationsPhaseOperationCollectionAspect extends
		MethodOperationCollectionAspect {

	static final OperationType TYPE = OperationType
			.valueOf("process_validations_phase_operation");

	public pointcut collectionPoint()
        : execution(boolean org.apache.myfaces.lifecycle.ProcessValidationsExecutor.execute(FacesContext))
        	|| execution (void com.sun.faces.lifecycle.ProcessValidationsPhase.execute(FacesContext));

	@Override
	protected Operation createOperation(JoinPoint jp) {
		return super.createOperation(jp).type(TYPE)
				.label("JSF Process Validations Phase");
	}
    @Override
    public String getPluginName() {
     	return JsfPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
