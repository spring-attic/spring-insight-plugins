package com.springsource.insight.plugin.jsf;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

public aspect PhaseListenerOperationCollectionAspect extends
		MethodOperationCollectionAspect {

	static final OperationType TYPE = OperationType.valueOf("jsf_phase_listener_operation");

	public pointcut collectionPoint()
        : (execution(public void PhaseListener.beforePhase(PhaseEvent))
            || execution(public void PhaseListener.afterPhase(PhaseEvent)))
            && !(within(com.sun.faces.**) || within(org.apache.myfaces.**));

	@Override
	protected Operation createOperation(JoinPoint jp) {
		PhaseEvent phaseEvent = (PhaseEvent) jp.getArgs()[0];
		PhaseId phaseId = phaseEvent.getPhaseId();

		StringBuilder label = new StringBuilder("JSF Phase Listener [");
		label.append(jp.getTarget().getClass().getSimpleName());
		label.append("#");
		label.append(jp.getSignature().getName());
		label.append("]");
		return super.createOperation(jp).type(TYPE).label(label.toString())
				.put("phaseId", phaseId.toString());
	}
    @Override
    public String getPluginName() {
     	return JsfPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
