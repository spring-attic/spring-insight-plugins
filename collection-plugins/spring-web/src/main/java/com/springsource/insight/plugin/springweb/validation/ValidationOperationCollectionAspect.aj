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

package com.springsource.insight.plugin.springweb.validation;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.plugin.springweb.ControllerPointcuts;
import com.springsource.insight.plugin.springweb.SpringWebPluginRuntimeDescriptor;

public aspect ValidationOperationCollectionAspect extends MethodOperationCollectionAspect {
	public ValidationOperationCollectionAspect () {
		super();
	}

    public pointcut collectionPoint() : ControllerPointcuts.validation();

    @Override
	protected Operation createOperation(JoinPoint jp) {
		return super.createOperation(jp)
					.put(EndPointAnalysis.SCORE_FIELD, EndPointAnalysis.CEILING_LAYER_SCORE)
					;
	}

	@Override
    public String getPluginName() {
        return SpringWebPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
