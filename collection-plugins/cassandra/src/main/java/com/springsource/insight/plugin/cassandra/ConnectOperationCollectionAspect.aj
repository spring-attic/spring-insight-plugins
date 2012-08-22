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

package com.springsource.insight.plugin.cassandra;


import org.aspectj.lang.JoinPoint;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.thrift.protocol.TProtocol;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;

/**
 * Collection operation for CassandraDB CQL queries
 */
public privileged aspect ConnectOperationCollectionAspect extends AbstractOperationCollectionAspect {
    public ConnectOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint() : execution(public Cassandra.Client org.apache.cassandra.thrift.Cassandra.Client.Factory.getClient(..)) ||
    									execution(public org.apache.cassandra.thrift.Cassandra.Client.new(..));

    @Override
    protected Operation createOperation(JoinPoint jp) {
    	Object[] args = jp.getArgs();

    	Operation operation = OperationUtils.createOperation(OperationCollectionTypes.CONNECT_TYPE, null, getSourceCodeLocation(jp)); 
		// get transport info
		String conn=OperationUtils.putTransportInfo(operation, (TProtocol)args[0]);
		operation.label(OperationCollectionTypes.CONNECT_TYPE.label+conn);
		
		return operation;
    }
    
	@Override
    public String getPluginName() {
		return CassandraPluginRuntimeDescriptor.PLUGIN_NAME;
	}
}
