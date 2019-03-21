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

package com.springsource.insight.plugin.cassandra;


import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.Compression;
import org.apache.cassandra.thrift.CqlPreparedResult;
import org.apache.cassandra.thrift.CqlResult;
import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.util.ListUtil;

/**
 * Collection operation for CassandraDB CQL queries
 */
public privileged aspect CQLOperationCollectionAspect extends AbstractCassandraOperationCollectionAspect {
    public CQLOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint() : execution(public CqlResult org.apache.cassandra.thrift.Cassandra.Client.execute_cql_query(ByteBuffer, Compression)) ||
    									execution(public CqlResult org.apache.cassandra.thrift.Cassandra.Client.execute_prepared_cql_query(int, List)) ||
    									execution(public CqlPreparedResult org.apache.cassandra.thrift.Cassandra.Client.prepare_cql_query(ByteBuffer, Compression));

    @Override
    protected Operation createOperation(JoinPoint jp) {
    	Object[] args = jp.getArgs();
    	String method=jp.getSignature().getName(); //method name
    	
		Operation operation = OperationUtils.createOperation(OperationCollectionTypes.CQL_TYPE, method, getSourceCodeLocation(jp)); 
		// get transport info
		OperationUtils.putTransportInfo(operation, ((Cassandra.Client)jp.getTarget()).getInputProtocol());

		// get query data
		if (!method.equals("execute_prepared_cql_query")) {
			String query=OperationUtils.getString((ByteBuffer)args[0]);
			operation.put("query", (query!=null)?query:"");
			operation.putAnyNonEmpty("compression", (args[1]!=null)?((Compression)args[1]).name():null);
		} else {
			operation.put("queryId",((Number)args[0]).intValue());
			@SuppressWarnings("unchecked")
			Collection<ByteBuffer> params=(Collection<ByteBuffer>)args[1];
			if (ListUtil.size(params) > 0 ) {
				OperationList opList=operation.createList("params");
				for(ByteBuffer param: params) {
					opList.add(OperationUtils.getString(param));
				}
			}
		}
		
		return operation;
    }
    
	@Override
    public String getPluginName() {
		return CassandraPluginRuntimeDescriptor.PLUGIN_NAME;
	}
}
