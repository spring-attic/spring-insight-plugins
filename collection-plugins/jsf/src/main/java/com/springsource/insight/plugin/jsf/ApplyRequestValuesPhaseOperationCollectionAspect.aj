package com.springsource.insight.plugin.jsf;

import java.util.Iterator;

import javax.faces.context.FacesContext;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;

public aspect ApplyRequestValuesPhaseOperationCollectionAspect extends
		MethodOperationCollectionAspect {

	static final OperationType TYPE = OperationType
			.valueOf("apply_request_values_phase_operation");

	public pointcut collectionPoint()
        : execution(boolean org.apache.myfaces.lifecycle.ApplyRequestValuesExecutor.execute(FacesContext))
        	|| execution(void com.sun.faces.lifecycle.ApplyRequestValuesPhase.execute(FacesContext));

	@Override
	protected Operation createOperation(JoinPoint jp) {
		FacesContext context = (FacesContext) jp.getArgs()[0];

		Operation phaseOperation = super.createOperation(jp).type(TYPE)
				.label("JSF Apply Request Values Phase");

		OperationMap requestParameters = phaseOperation
				.createMap("requestParameters");
		Iterator<String> requestParametersIterator = context
				.getExternalContext().getRequestParameterMap().keySet()
				.iterator();
		while (requestParametersIterator.hasNext()) {
			String key = requestParametersIterator.next();
			requestParameters.put(key, context.getExternalContext()
					.getRequestParameterMap().get(key));
		}

		OperationMap requestHeaders = phaseOperation
				.createMap("requestHeaders");
		Iterator<String> requestHeadersIterator = context.getExternalContext()
				.getRequestHeaderMap().keySet().iterator();
		while (requestHeadersIterator.hasNext()) {
			String key = requestHeadersIterator.next();
			requestHeaders.put(key, context.getExternalContext()
					.getRequestHeaderMap().get(key));
		}

		return phaseOperation;
	}
    @Override
    public String getPluginName() {
       	return JsfPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
