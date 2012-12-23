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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.errorhandling.CollectionErrors;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;

public aspect JdbcPreparedStatementOperationCollectionAspect
    extends OperationCollectionAspectSupport 
{
    /**
     * The keys and values of this should be strongly referenced in the modified class instance
     * and the frame respectively, so they should not be prematurely removed.
     */
    private final WeakKeyHashMap<PreparedStatement, Operation> storage = new WeakKeyHashMap<PreparedStatement, Operation>();
    
    public JdbcPreparedStatementOperationCollectionAspect () {
    	super();
    }

    public pointcut metaDataRetrieval() : execution(* java.sql.Connection.getMetaData());
    public pointcut fetchDatabaseUrl() : execution(* java.sql.DatabaseMetaData.getURL());

    /* Select PreparedStatement's execute(), executeUpdate(), and executeQuery()
     * methods -- none of them take any parameters. Although, PreparedStatement
     * is a Statement, therefore, has execute*(String, ..) methods, we don't select
     * them here. Such methods don't follow the spirit of a PreparedStatement, 
     * therefore, we treat them as plain Statement and select them in 
     * JdbcStatementMetricCollectionAspect.
     */
    public pointcut execute() 
        :  execution(* java.sql.PreparedStatement.execute*())
        && collect();

    public pointcut collect()
        :  if (strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
   // avoid collecting SQL queries due to meta-data retrieval since it would cause infinite recursion
     && (!cflow(metaDataRetrieval()))
     && (!cflow(fetchDatabaseUrl()))
        ;

    pointcut preparedStatementCreation(String sql) 
        : collect()
        && ((execution(PreparedStatement Connection.prepareStatement(String, ..))
          || execution(CallableStatement Connection.prepareCall(String, ..)))
        && args(sql, ..))
         ;

    pointcut preparedStatementSetParameter(PreparedStatement statement, int index, Object parameter)
        : collect()
       && execution(public void PreparedStatement.set*(int, *))
       && this(statement)
       && args(index, parameter)
        ;
    
    pointcut callableStatementSetParameter(CallableStatement statement, String key, Object parameter)
        : collect()
       && execution(public void CallableStatement.set*(String, *))
       && this(statement)
       && args(key, parameter)
       ;

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after(String sql) returning(PreparedStatement statement) : preparedStatementCreation(sql) {
        createOperationForStatement(thisJoinPoint, statement, sql);
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after(PreparedStatement statement, int index, Object parameter) returning
        : preparedStatementSetParameter(statement, index, parameter) 
    {
        Operation operation = getOperationForStatement(statement);
        if (operation != null) {
            JdbcOperationFinalizer.addParam(operation, index, parameter);
        }
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after(PreparedStatement statement, String parameterName, Object parameter) returning
        : callableStatementSetParameter(statement, parameterName, parameter) 
    {
        Operation operation = getOperationForStatement(statement);
        if (operation != null) {
            JdbcOperationFinalizer.addParam(operation, parameterName, parameter);
        }
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    before(): execute() {
        /**
         * We only want to add operations for prepared statements that we actually
         * collected the SQL for (via a prepareStatement or prepareCall)
         */
        PreparedStatement thisStatement = (PreparedStatement) thisJoinPoint.getThis();
        Operation op = getOperationForStatement(thisStatement);
        if (op != null) {
            getCollector().enter(op);
        } else {
            // stmt.execute() called, but stmt was never returned via a prepareStatement().
            // possibly someone wrapping a preparedStatement (delegation)
        }
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after() returning(Object returnValue): execute() {

        PreparedStatement thisStatement = (PreparedStatement) thisJoinPoint.getThis();
        Operation op = getOperationForStatement(thisStatement);

        if (op != null) {
            getCollector().exitNormal(returnValue);
            // removing the softkey entry here actually appears to *degrade* performance
            // This may be because the entire object, including "storage" is thrown away
            // anyways.
        }
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after() throwing(Throwable exception): execute() {
        PreparedStatement thisStatement = (PreparedStatement) thisJoinPoint.getThis();
        Operation op = getOperationForStatement(thisStatement);

        if (op != null) {
            getCollector().exitAbnormal(exception);
            // See the note above for exitNormal for why we do not explicitly remove
            // the SoftKeyEntries here
        }
    }

    Operation createOperationForStatement(JoinPoint jp, PreparedStatement statement, String sql) {
        Operation operation = new Operation()
                .type(JdbcOperationExternalResourceAnalyzer.TYPE)
                .sourceCodeLocation(getSourceCodeLocation(jp))
                .put("sql", sql)
                ;

        // always return an operation
        try {
            JdbcOperationFinalizer.register(operation);
            addStatementToMap(statement, operation);

            Connection	connection = statement.getConnection();
            DatabaseMetaData	metaData = connection.getMetaData();
            operation.putAnyNonEmpty(OperationFields.CONNECTION_URL, metaData.getURL());
        } catch(SQLException e) {
            // ignore, possibly expected
        } catch (Throwable t) {
            CollectionErrors.markCollectionError(this.getClass(), t);
        }
        return operation;
    }
    
    private Operation getOperationForStatement(PreparedStatement statement) {
        return storage.get(statement);
    }
    
    private void addStatementToMap(PreparedStatement ps, Operation op) {
        storage.put(ps, op);
    }

    @Override
    public String getPluginName() {
        return JdbcRuntimePluginDescriptor.PLUGIN_NAME;
    }
}
