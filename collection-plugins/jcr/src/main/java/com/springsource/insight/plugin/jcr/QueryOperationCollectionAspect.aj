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

package com.springsource.insight.plugin.jcr;

import org.aspectj.lang.JoinPoint;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;

/**
 * This aspect intercepts all JCR Query requests
 */
public privileged aspect QueryOperationCollectionAspect extends AbstractOperationCollectionAspect {
	public QueryOperationCollectionAspect () {
		super();
	}

    public pointcut collectionPoint() : execution(public QueryResult javax.jcr.query.Query+.execute());

    @Override
    protected Operation createOperation(JoinPoint jp) {
    	Query query=(Query)jp.getTarget();
    			
    	return new Operation().type(OperationCollectionTypes.QUERY_TYPE.type)
    						.label(OperationCollectionTypes.QUERY_TYPE.label)
    						.sourceCodeLocation(getSourceCodeLocation(jp))
    						.put("statement", query.getStatement())
    						;
    }

	@Override
	public String getPluginName() {
		return JCRPluginRuntimeDescriptor.PLUGIN_NAME;
	}
}