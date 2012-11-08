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

package com.springsource.insight.plugin.eclipse.persistence;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.method.JoinPointFinalizer;
import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.util.StringUtil;

/**
 * 
 */
public abstract aspect EclipsePersistenceCollectionAspect extends MethodOperationCollectionAspect {
	protected final OperationType	opType;
	protected final String			labelPrefix;

	protected EclipsePersistenceCollectionAspect (OperationType type, String lblPrefix) {
		this(JoinPointFinalizer.getJoinPointFinalizerInstance(), type, lblPrefix);
	}

	protected EclipsePersistenceCollectionAspect (JoinPointFinalizer finalizerInstance, OperationType type, String lblPrefix) {
		super(finalizerInstance);

		if ((opType=type) == null) {
			throw new IllegalStateException("No operation type provided");
		}
		
		if (StringUtil.isEmpty(lblPrefix)) {
			throw new IllegalStateException("No label prefix provided");
		}

		labelPrefix = lblPrefix;
	}

    @Override
    public String getPluginName() {
        return EclipsePersistenceDefinitions.PLUGIN_NAME;
    }

    protected Operation createOperation(JoinPoint jp, String actionName) {
    	return super.createOperation(jp)
    				.type(opType)
    				.label(labelPrefix + " " + actionName)
                    .put(EclipsePersistenceDefinitions.ACTION_ATTR, actionName)
    				;
    }
}
