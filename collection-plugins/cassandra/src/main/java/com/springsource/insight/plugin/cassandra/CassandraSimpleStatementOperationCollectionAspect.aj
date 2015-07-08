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


import com.datastax.driver.core.BoundStatement;
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.datastax.driver.core.SimpleStatement;
import com.springsource.insight.intercept.operation.Operation;
import org.aspectj.lang.JoinPoint;

public aspect CassandraSimpleStatementOperationCollectionAspect
        extends OperationCollectionAspectSupport {

    public CassandraSimpleStatementOperationCollectionAspect() {
        super();
    }


    public pointcut collect()
        :  if (strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
        ;

    pointcut simpleStatementCreation(SimpleStatement statement, String query, Object[] parameters)
        : collect()
        && execution(SimpleStatement.new(String,Object...))
        && this(statement)
        && args(query,parameters)
        ;

    after(SimpleStatement statement, String query, Object[] parameters)
            : simpleStatementCreation(statement,query,parameters) {

        Operation operation = createOperationForStatement(thisJoinPoint, statement, query);

        int index = 0;
        for(Object parameter: parameters) {
            CassandraOperationFinalizer.addParam(operation, index, parameter);
            index++;
        }

    }

    @Override
    public String getPluginName() {
        return CassandraRuntimePluginDescriptor.PLUGIN_NAME;
    }

    @Override
    public boolean isMetricsGenerator() {
        return true; // This provides an external resource
    }

    private Operation createOperationForStatement(JoinPoint jp, SimpleStatement statement, String cql) {

        Operation operation = new Operation()
                .type(CassandraExternalResourceAnalyzer.TYPE)
                .sourceCodeLocation(getSourceCodeLocation(jp))
                .label(CassandraOperationFinalizer.createLabel(cql))
                .put("cql", cql);

        CassandraOperationFinalizer.put(statement, operation);

        return operation;
    }


}
