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
package com.springsource.insight.plugin.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.StringUtil;

public aspect JdbcStatementOperationCollectionAspect extends AbstractOperationCollectionAspect {
    public JdbcStatementOperationCollectionAspect () {
    	super();
    }

    public pointcut sqlQueryExecution() : execution(* java.sql.Statement.execute*(String, ..));
    public pointcut metaDataRetrieval() : execution(* java.sql.Connection.getMetaData());
    public pointcut fetchDatabaseUrl() : execution(* java.sql.DatabaseMetaData.getURL());

    public pointcut collectionPoint() 
        : sqlQueryExecution()
      // avoid collecting SQL queries due to meta-data retrieval since it would cause infinite recursion
      && (!cflow(metaDataRetrieval()))
      && (!cflow(fetchDatabaseUrl()))
        ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
    	Object[]	args=jp.getArgs();
    	String		sql=(ArrayUtil.length(args) <= 0) ? "<UNKNOWN>" : StringUtil.safeToString(args[0]);
        Operation 	operation = new Operation()
            .type(JdbcOperationExternalResourceAnalyzer.TYPE)
            .sourceCodeLocation(getSourceCodeLocation(jp))
            .label(JdbcOperationFinalizer.createLabel(sql))
            .putAnyNonEmpty("sql", sql)
            ;
        try {
            Statement 	statement = (Statement) jp.getTarget();
            Connection	connection = statement.getConnection();
            DatabaseMetaData	metaData = connection.getMetaData();
            operation.putAnyNonEmpty(OperationFields.CONNECTION_URL, metaData.getURL());            
        } catch (SQLException e) {
            // ignore
        }
        return operation;
    }

    @Override
    public String getPluginName() {
        return JdbcRuntimePluginDescriptor.PLUGIN_NAME;
    }
    
    @Override
    public boolean isMetricsGenerator() {
        return true; // This provides an external resource
    }
}
