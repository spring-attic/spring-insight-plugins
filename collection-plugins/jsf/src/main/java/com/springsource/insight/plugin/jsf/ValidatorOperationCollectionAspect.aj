package com.springsource.insight.plugin.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

public aspect ValidatorOperationCollectionAspect extends
		MethodOperationCollectionAspect {

	static final OperationType TYPE = OperationType.valueOf("jsf_validator_operation");

	public pointcut collectionPoint()
        : execution(public void Validator.validate(FacesContext, UIComponent, Object));

	@Override
	protected Operation createOperation(JoinPoint jp) {
		UIComponent uiComponent = (UIComponent) jp.getArgs()[1];
		Object object = jp.getArgs()[2];

		StringBuilder label = new StringBuilder("JSF Validator [");
		label.append(jp.getTarget().getClass().getSimpleName());
		label.append("]");
		return super.createOperation(jp).type(TYPE).label(label.toString())
				.put("uiComponentId", uiComponent.getId())
				.put("value", object.toString());
	}
    @Override
    public String getPluginName() {
     	return JsfPluginRuntimeDescriptor.PLUGIN_NAME;
    }

}
