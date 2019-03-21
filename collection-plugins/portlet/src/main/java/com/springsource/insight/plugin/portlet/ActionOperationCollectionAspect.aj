/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.insight.plugin.portlet;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.ProcessAction;

import org.aspectj.lang.JoinPoint;
import com.springsource.insight.intercept.operation.Operation;

/**
 * This aspect create insight operation for Portlet activity
 * @type: portlet-action
 */
public privileged aspect ActionOperationCollectionAspect extends GenericOperationCollectionAspect {
    public ActionOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint(): execution(void javax.portlet.Portlet+.processAction(ActionRequest, ActionResponse)) ||
            execution(@ProcessAction void *(ActionRequest, ActionResponse));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[] args = jp.getArgs();
        ActionRequest req = (ActionRequest) args[0];

        Operation op = createOperation(jp, OperationCollectionTypes.ACTION_TYPE);
        try {
            //portlet2 support
            op.putAnyNonEmpty("actionName", req.getParameter(ActionRequest.ACTION_NAME))
                    .putAnyNonEmpty("actionPhase", req.getParameter(PortletRequest.ACTION_PHASE))
            ;
        } catch (Error e) {
            // ignored
        }

        return op;
    }
}
