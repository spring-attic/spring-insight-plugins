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

package com.springsource.insight.plugin.hadoop;


import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;

/**
 * Collection operation for Hadoop Reducer 
 */
public privileged aspect ReduceOperationCollectionAspect extends AbstractOperationCollectionAspect {
    public ReduceOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint() : execution(public void org.apache.hadoop.mapred.Reducer+.reduce(..)) ||
    									execution(protected void org.apache.hadoop.mapreduce.Reducer+.reduce(..));

    @Override
    protected Operation createOperation(JoinPoint jp) {
    	Object[] args = jp.getArgs();
    	
		Operation operation = new Operation().type(OperationCollectionTypes.REDUCE_TYPE.type)
    						.label(OperationCollectionTypes.REDUCE_TYPE.label)
    						.sourceCodeLocation(getSourceCodeLocation(jp));

		operation.putAnyNonEmpty("key", args[0].toString());
		
		/*Iterator<?> values;
		if (args[1] instanceof Iterable) {
			values=((Iterable<?>)args[1]).iterator();
		}
		else {
			values=(Iterator<?>)args[1];
		}
		
		if (values!=null && values.hasNext() && values.getClass().getName().indexOf("Mock")==-1) {
			OperationList list=operation.createList("values");
			while (values.hasNext()) {
				list.add(values.next().toString());
			}
		}*/

		return operation;
    }
    
	@Override
    public String getPluginName() {
		return HadoopPluginRuntimeDescriptor.PLUGIN_NAME;
	}
}
