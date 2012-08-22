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
import org.apache.cassandra.thrift.AuthenticationRequest;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.CfDef;
import org.apache.cassandra.thrift.ColumnDef;
import org.apache.cassandra.thrift.KsDef;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;


/**
 * Collection operation for CassandraDB system commands 
 */
public privileged aspect SystemOperationCollectionAspect extends AbstractOperationCollectionAspect {
    public SystemOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint() : execution(public String org.apache.cassandra.thrift.Cassandra.Client.system_*(..)) ||
    									execution(public void org.apache.cassandra.thrift.Cassandra.Client.set_keyspace(String)) ||
    									execution(public void org.apache.cassandra.thrift.Cassandra.Client.login(AuthenticationRequest));

    @Override
    protected Operation createOperation(JoinPoint jp) {
    	Object[] args = jp.getArgs();
    	String method=jp.getSignature().getName(); //method name
    	
		Operation operation = OperationUtils.createOperation(OperationCollectionTypes.SYSTEM_TYPE, method, getSourceCodeLocation(jp)); 
		// get transport info
		OperationUtils.putTransportInfo(operation, ((Cassandra.Client)jp.getTarget()).getInputProtocol());

		if ((args[0] instanceof CfDef) && (args[0]!=null)) {
			CfDef cfdef=(CfDef)args[0];
			operation.put("keyspace", OperationUtils.getText(cfdef.getKeyspace()));
			
			OperationMap opMap=operation.createMap("columnFamilyDef");
			opMap.put("name", cfdef.getName());
			opMap.put("type", cfdef.getColumn_type());
			opMap.putAnyNonEmpty("class", cfdef.getKey_validation_class());
			 
			if (cfdef.getColumn_metadata()!=null) {
				OperationList cols=opMap.createList("columnsDef");
				for (ColumnDef col: cfdef.getColumn_metadata()) {
					cols.add(OperationUtils.getText(col.getName())+":"+col.getValidation_class());
				}
			}
		}
		else
		if ((args[0] instanceof KsDef) && (args[0]!=null)) {
			KsDef ksdef=(KsDef)args[0];
			operation.put("keyspace", OperationUtils.getText(ksdef.getName()));
			operation.put("class", OperationUtils.getText(ksdef.getStrategy_class()));
		}
		else
		if (method.endsWith("drop_keyspace") || method.equals("set_keyspace")) {
			operation.put("keyspace", OperationUtils.getText((String)args[0]));
		}
		else
		if (method.endsWith("drop_column_family")) {
			operation.put("columnFamily", OperationUtils.getText((String)args[0]));
		}
		else
		if (method.equals("login")) {
			if (args[0]!=null) {
				OperationMap map=operation.createMap("credentials");
				map.putAnyAll(((AuthenticationRequest)args[0]).getCredentials());
			}		
		}
		
		return operation;
    }
    
	@Override
    public String getPluginName() {
		return CassandraPluginRuntimeDescriptor.PLUGIN_NAME;
	}
}
