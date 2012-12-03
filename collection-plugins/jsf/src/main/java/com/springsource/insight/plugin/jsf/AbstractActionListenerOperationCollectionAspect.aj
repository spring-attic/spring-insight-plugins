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

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.MethodInfo;
import javax.el.ValueExpression;
import javax.faces.component.ActionSource;
import javax.faces.component.ActionSource2;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.ActionEvent;
import javax.faces.event.MethodExpressionActionListener;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;


@SuppressWarnings("deprecation")
public abstract aspect AbstractActionListenerOperationCollectionAspect extends AbstractJSFOperationCollectionAspect {

    static final OperationType TYPE = OperationType.valueOf("jsf_action_listener_operation");

    @Override
    protected Operation createOperation(JoinPoint jp) {
        ActionEvent actionEvent = (ActionEvent) jp.getArgs()[0];

        FacesContext ctx = FacesContext.getCurrentInstance();
        ELContext elContext = ctx.getELContext();

        String fromAction = null;
        MethodInfo methodInfo = null;
        MethodExpression methodExpression = null;

        if (jp.getTarget() instanceof MethodExpressionActionListener) {
            MethodExpressionActionListener listener = (MethodExpressionActionListener) jp.getTarget();
            Object state = listener.saveState(ctx);
            if (state == null) {
                state = loadState(ctx, listener);
            }
            if (state instanceof Object[]) {
                Object[] expressions = (Object[]) state;
                if (expressions.length > 0 && expressions[0] instanceof MethodExpression) {
                    methodExpression = (MethodExpression) expressions[0];
                    fromAction = methodExpression.getExpressionString();
                    methodInfo = methodExpression.getMethodInfo(elContext);
                }
            }
        } else {
            UIComponent component = actionEvent.getComponent();
    
            if (component instanceof ActionSource2) {
                // Must be an instance of ActionSource2, so don't look on action
                // if the actionExpression is set
                methodExpression = ((ActionSource2) component).getActionExpression();
                if (methodExpression != null && !methodExpression.isLiteralText()) {
                    fromAction = methodExpression.getExpressionString();
                    methodInfo = methodExpression.getMethodInfo(elContext);
                }
            } 
            if (methodExpression == null && component instanceof ActionSource) {
                // Backwards compatibility for pre-1.2.
                MethodBinding methodBinding = ((ActionSource) component).getAction();
                if (methodBinding != null && methodBinding.getExpressionString() != null
                        && methodBinding.getExpressionString().startsWith("#{")) {
                    fromAction = methodBinding.getExpressionString();
                    methodInfo = new MethodInfo(getBeanMethodName(fromAction), methodBinding.getType(ctx), null);
                }
            }
        }
        
        if (fromAction == null) {
            fromAction = "No Action";
        }
        
        StringBuilder label = new StringBuilder("JSF Action [");
		label.append(fromAction);
		label.append("]");
        Operation operation = super.createOperation(jp).type(TYPE).label(label.toString());
        if (fromAction != null) {
            Class<?> implementationClass = getBeanClass(ctx, elContext, fromAction);
            operation.put("implementationClass", implementationClass != null ? implementationClass.getName() : "unknown")
                        .put("implementationClassMethod", methodInfo != null ? methodInfo.getName() : "unknown")
                        .put("implementationClassMethodSignature", methodInfo != null ? getBeanMethodSignature(methodInfo) : "unknown()")
                        .put("fromAction", fromAction);
        }
        return operation;
    }

    protected abstract Object loadState(FacesContext ctx, MethodExpressionActionListener listener);
    
    protected Class<?> getBeanClass(FacesContext ctx, ELContext elContext, String expression) {
        Class<?> toReturn = null;
        int index = expression.lastIndexOf('.');

        if (index > -1) {
            String beanName = expression.substring(0, index);
            ValueExpression valueExpression = ctx.getApplication().getExpressionFactory().createValueExpression(elContext, beanName + "}", Object.class);
            Object bean = valueExpression.getValue(elContext);
            toReturn = bean != null ? bean.getClass() : null;
        }

        return toReturn;
    }

    protected String getBeanMethodName(String expression) {
        String result = null;
        int index = expression.lastIndexOf('.');
        if (index > -1) {
            result = expression.substring(index + 1, expression.length() - 1);
        }
        return result;
    }
    
    protected String getBeanMethodSignature(MethodInfo methodInfo) {
        StringBuilder result = new StringBuilder();
        if (methodInfo.getReturnType() != null) {
            result.append(methodInfo.getReturnType().getSimpleName());
        } else {
            result.append("void");
        }
        result.append(" ");
        result.append(methodInfo.getName());
        result.append("(");
        if (methodInfo.getParamTypes() != null && methodInfo.getParamTypes().length > 0) {
            for (int i = 0; i < methodInfo.getParamTypes().length; i++) {
                result.append(methodInfo.getParamTypes()[i].getSimpleName());
                if (i < methodInfo.getParamTypes().length - 1) {
                    result.append(",");
                }
            }
        }
        result.append(")");
        return result.toString();
    }
    
    @Override
    public boolean isEndpoint() {
    	return true;
    }
}
