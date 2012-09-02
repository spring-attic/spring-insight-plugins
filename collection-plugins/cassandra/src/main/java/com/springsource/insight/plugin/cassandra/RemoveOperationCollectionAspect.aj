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

package com.springsource.insight.plugin.cassandra;


import org.aspectj.lang.JoinPoint;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.ColumnPath;
import org.apache.cassandra.thrift.ConsistencyLevel;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;

import java.nio.ByteBuffer;

/**
 * Collection operation for CassandraDB CQL queries
 */
public privileged aspect RemoveOperationCollectionAspect extends AbstractOperationCollectionAspect {
    public RemoveOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint() : 
    	execution(public void org.apache.cassandra.thrift.Cassandra.Client.remove_counter(ByteBuffer, ColumnPath, ConsistencyLevel)) ||
    	execution(public void org.apache.cassandra.thrift.Cassandra.Client.remove(ByteBuffer, ColumnPath, long, ConsistencyLevel)) ||
    	execution(public void org.apache.cassandra.thrift.Cassandra.Client.truncate(String));

    @Override
    protected Operation createOperation(JoinPoint jp) {
    	String method=jp.getSignature().getName(); //method name
    	Object[] args = jp.getArgs();
    	
		Operation operation = OperationUtils.createOperation(OperationCollectionTypes.REMOVE_TYPE, method, getSourceCodeLocation(jp)); 
		// get transport info
		OperationUtils.putTransportInfo(operation, ((Cassandra.Client)jp.getTarget()).getInputProtocol());

		if (!method.equals("truncate")) {
			operation.put("key", OperationUtils.getText((ByteBuffer)args[0]));
			
			ColumnPath colPath=(ColumnPath)args[1];
			if (colPath!=null) {
				operation.put("columnFamily", OperationUtils.getText(colPath.getColumn_family()));
				operation.putAnyNonEmpty("superColumn", OperationUtils.getString(colPath.getSuper_column()));
				operation.putAnyNonEmpty("colName", OperationUtils.getString(colPath.getColumn()));
			}
			operation.putAnyNonEmpty("consistLevel", (args[args.length-1]!=null)?((ConsistencyLevel)args[args.length-1]).name():null);
			
			if (method.equals("remove")) {
				operation.put("timestamp", ((Number)args[2]).longValue());
			}
		}
		else {
			operation.put("columnFamily", OperationUtils.getText((String)args[0]));
		}		
		
		return operation;
    }
    
	@Override
    public String getPluginName() {
		return CassandraPluginRuntimeDescriptor.PLUGIN_NAME;
	}
}
