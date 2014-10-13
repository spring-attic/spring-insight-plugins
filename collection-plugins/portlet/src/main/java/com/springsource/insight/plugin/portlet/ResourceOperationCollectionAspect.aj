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

package com.springsource.insight.plugin.portlet;

import javax.portlet.PortletRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;

/**
 * This aspect create insight operation for Portlet resource
 * @type: portlet-resource
 */
public privileged aspect ResourceOperationCollectionAspect extends GenericOperationCollectionAspect {
    public ResourceOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint(): execution(void javax.portlet.ResourceServingPortlet+.serveResource(ResourceRequest, ResourceResponse));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[] args = jp.getArgs();
        ResourceRequest req = (ResourceRequest) args[0];

        return createOperation(jp, OperationCollectionTypes.RESOURCE_TYPE)
                .putAnyNonEmpty("ETag", req.getETag())
                .putAnyNonEmpty("resourceCacheability", req.getCacheability())
                .putAnyNonEmpty("resourcePhase", req.getParameter(PortletRequest.RESOURCE_PHASE))
                ;
    }
}
