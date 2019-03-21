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

package com.springsource.insight.plugin.jpa;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public abstract aspect JpaEntityManagerCollectionAspect extends MethodOperationCollectionAspect {
    protected final String  opGroup;

    protected JpaEntityManagerCollectionAspect (@SuppressWarnings("hiding") String opGroup) {
        if (((this.opGroup=opGroup) == null) || (opGroup.length() <= 0)) {
            throw new IllegalStateException("No operation group specified");
        }
    }

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Signature   sig=jp.getSignature();
        return super.createOperation(jp)
                    .type(JpaDefinitions.ENTITY_MGR)
                    .put(JpaDefinitions.ACTION_ATTR, sig.getName())
                    .put(JpaDefinitions.GROUP_ATTR, this.opGroup)
                    ;
    }

    @Override
    public String getPluginName() {return JpaPluginRuntimeDescriptor.PLUGIN_NAME;}
}
