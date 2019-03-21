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


import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.BoundStatement;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.errorhandling.CollectionErrors;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;

public aspect CassandraBoundStatementOperationCollectionAspect
        extends OperationCollectionAspectSupport {

    public CassandraBoundStatementOperationCollectionAspect() {
        super();
    }

    public pointcut collect()
            :  if (strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
            ;

    pointcut boundStatementCreationParameters(Object[] parameters)
            : collect()
            && execution(BoundStatement PreparedStatement.bind(java.lang.Object...))
            && args(parameters)
            ;
    pointcut boundStatementBindParameters(Object[] parameters)
            : collect()
            && execution(BoundStatement BoundStatement.bind(java.lang.Object...))
            && args(parameters)
            ;
    pointcut boundStatementSetByIndexParameter(int index, Object parameter)
            : collect()
            && execution(BoundStatement BoundStatement.set*(int,*))
            && args(index, parameter)
            ;
    pointcut boundStatementSetByNameParameter(String key, Object parameter)
            : collect()
            && execution(BoundStatement BoundStatement.set*(java.lang.String,*))
            && args(key, parameter)
            ;

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after(Object[] parameters) returning(BoundStatement result)
            : ( boundStatementCreationParameters(parameters)
            || boundStatementBindParameters(parameters) ) {

        Operation operation = CassandraOperationFinalizer.get(result);
        if (operation == null)
            operation = createOperationForStatement(thisJoinPoint, result);

        int index = 0;
        for(Object parameter: parameters) {
            CassandraOperationFinalizer.addParam(operation, index, parameter);
            index++;
        }
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after(int index, Object parameter) returning(BoundStatement result)
            : boundStatementSetByIndexParameter(index, parameter) {
        Operation operation = CassandraOperationFinalizer.get(result);
        if (operation == null)
            operation = createOperationForStatement(thisJoinPoint, result);
        CassandraOperationFinalizer.addParam(operation, index, parameter);
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after(String parameterName, Object parameter) returning(BoundStatement result)
            : boundStatementSetByNameParameter(parameterName, parameter) {
        Operation operation = CassandraOperationFinalizer.get(result);
        if (operation == null)
            operation = createOperationForStatement(thisJoinPoint, result);
        CassandraOperationFinalizer.addParam(operation, parameterName, parameter);

    }

    @Override
    public String getPluginName() {
        return CassandraRuntimePluginDescriptor.PLUGIN_NAME;
    }

    @Override
    public boolean isMetricsGenerator() {
        return true; // This provides an external resource
    }


    private Operation createOperationForStatement(JoinPoint jp, BoundStatement statement) {

        String cql = statement.preparedStatement().getQueryString();
        Operation operation = new Operation()
                .type(CassandraExternalResourceAnalyzer.TYPE)
                .sourceCodeLocation(getSourceCodeLocation(jp))
                .label(CassandraOperationFinalizer.createLabel(cql))
                .put("cql", cql);
        CassandraOperationFinalizer.put(statement, operation);

        return operation;
    }
}
