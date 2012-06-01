/**
 * Copyright 2009-2011 the original author or authors.
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

package com.springsource.insight.plugin.webflow;

import org.aspectj.lang.JoinPoint;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.RequestControlContext;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;

/**
 * This aspect create insight operation for Webflow Start activity
 * @type: wf-start
 * @properties: flowId, initParams<String,Object>
 */
public aspect StartOperationCollectionAspect extends AbstractOperationCollectionAspect {
    public StartOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint() 
    	: execution(void org.springframework.webflow.engine.impl.FlowExecutionImpl.start(Flow, MutableAttributeMap, RequestControlContext));

    @Override
    @SuppressWarnings("unchecked")
	protected Operation createOperation(JoinPoint jp) {
    	Object[] args = jp.getArgs();
    	Flow flow = (Flow)args[0];
    	MutableAttributeMap params=(MutableAttributeMap)args[1];
        
    	Operation  operation = new Operation().type(OperationCollectionTypes.START_TYPE.type)
    										.label(OperationCollectionTypes.START_TYPE.label+" ["+flow.getId()+"]")
    										.sourceCodeLocation(getSourceCodeLocation(jp));
            
        operation.put("flowId", flow.getId());
        
        if (params!=null && !params.isEmpty()) {
        	operation.createMap("initParams").putAnyAll(params.asMap());
        }
        
        return operation;
    }

    @Override
    public String getPluginName() {
        return "webflow";
    }
}
