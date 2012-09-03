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
package com.springsource.insight.plugin.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;

public aspect JdbcStatementOperationCollectionAspect 
    extends AbstractOperationCollectionAspect {
    
    public pointcut collectionPoint() 
        : execution(* java.sql.Statement.execute*(String, ..));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Operation operation = new Operation()
            .type(JdbcOperationExternalResourceAnalyzer.TYPE)
            .sourceCodeLocation(getSourceCodeLocation(jp))
            .put("sql", (String)jp.getArgs()[0]);
        JdbcOperationFinalizer.register(operation);
        try {
            Statement 	statement = (Statement) jp.getTarget();
            Connection	connection = statement.getConnection();
            DatabaseMetaData	metaData = connection.getMetaData();
            operation.put(OperationFields.CONNECTION_URL, metaData.getURL());            
        } catch (SQLException e) {
            // ignore
        }
        return operation;
    }

    @Override
    public String getPluginName() {
        return JdbcRuntimePluginDescriptor.PLUGIN_NAME;
    }
}
