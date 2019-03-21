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


import org.aspectj.lang.JoinPoint;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.CounterColumn;
import org.apache.cassandra.thrift.Mutation;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * Collection operation for CassandraDB CQL queries
 */
public privileged aspect UpdateOperationCollectionAspect extends AbstractCassandraOperationCollectionAspect {
    public UpdateOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint():
            execution(public void org.apache.cassandra.thrift.Cassandra.Client.add(ByteBuffer,ColumnParent,CounterColumn,ConsistencyLevel)) ||
                    execution(public void org.apache.cassandra.thrift.Cassandra.Client.insert(ByteBuffer,ColumnParent,Column,ConsistencyLevel)) ||
                    execution(public void org.apache.cassandra.thrift.Cassandra.Client.batch_mutate(Map,ConsistencyLevel));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        String method = jp.getSignature().getName(); //method name
        Object[] args = jp.getArgs();

        Operation operation = OperationUtils.createOperation(OperationCollectionTypes.UPDATE_TYPE, method, getSourceCodeLocation(jp));
        // get transport info
        OperationUtils.putTransportInfo(operation, ((Cassandra.Client) jp.getTarget()).getInputProtocol());

        operation.putAnyNonEmpty("consistLevel", (args[args.length - 1] != null) ? ((ConsistencyLevel) args[args.length - 1]).name() : null);
        if (!method.equals("batch_mutate")) {
            operation.put("key", OperationUtils.getText((ByteBuffer) args[0]));

            ColumnParent colParent = (ColumnParent) args[1];
            if (colParent != null) {
                operation.put("columnFamily", OperationUtils.getText(colParent.getColumn_family()));
                operation.putAnyNonEmpty("superColumn", OperationUtils.getString(colParent.getSuper_column()));
            }

            if (method.equals("add")) {
                CounterColumn colCounter = (CounterColumn) args[2];
                if (colCounter != null) {
                    operation.put("colName", OperationUtils.getText(colCounter.getName()));
                    operation.put("colValue", colCounter.getValue());
                }
            } else if (method.equals("insert")) {
                Column col = (Column) args[2];
                if (col != null) {
                    operation.put("colName", OperationUtils.getText(col.getName()));
                    operation.put("colValue", OperationUtils.getAnyData(col.getValue()));
                    operation.put("colTimestamp", col.getTimestamp());
                }
            }
        } else {
            @SuppressWarnings("unchecked")
            Map<ByteBuffer, Map<String, List<Mutation>>> mutation_map = (Map<ByteBuffer, Map<String, List<Mutation>>>) args[0];
            if (mutation_map != null && !mutation_map.isEmpty()) {
                //map<key : string, map<column_family : string, vector<Mutation>>>
                OperationMap keys = operation.createMap("tables");
                for (ByteBuffer bkey : mutation_map.keySet()) {
                    String tables = "";
                    for (String table : mutation_map.get(bkey).keySet()) {
                        tables += table + ",";
                    }
                    keys.put(OperationUtils.getString(bkey), (tables.length() > 0) ? tables.substring(0, tables.length() - 1) : "");
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
