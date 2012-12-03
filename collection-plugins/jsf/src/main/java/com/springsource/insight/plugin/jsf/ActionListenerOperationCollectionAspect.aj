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

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.MethodExpressionActionListener;

import com.springsource.insight.intercept.operation.OperationType;

public aspect ActionListenerOperationCollectionAspect extends AbstractActionListenerOperationCollectionAspect {

    static final OperationType TYPE = OperationType.valueOf("jsf_action_listener_operation");

    public pointcut collectionPoint()
        : execution(public void ActionListener.processAction(ActionEvent))
            && !(within(com.sun.faces.facelets.tag.jsf.core.ActionListenerHandler)
                    || within(org.apache.myfaces.application.ActionListenerImpl));

    protected Object loadState(FacesContext ctx, MethodExpressionActionListener listener) {
        return null;
    }
}
