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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.errorhandling.CollectionErrors;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;

/**
 * <P>Intercepts execution of queries via {@link PreparedStatement}s or
 * {@link CallableStatement}s. The expected usage model of the aspect is:</P></BR>
 * <UL>
 *
 * 		<LI>
 * 		A {@link PreparedStatement} or {@link CallableStatement} is created via
 * 		call(s) to {@link Connection#prepareStatement(String)}s or
 *        {@link Connection#prepareCall(String)}s.
 * 		</LI>
 *
 * 		<LI>
 * 		The statement is initialized via calls to <code>setXXX</code> parameter
 * 		calls.
 * 		</LI>
 *
 * 		</LI>
 * 		The <code>execute</code> method is called
 * 		</LI>
 *
 * 		<LI>
 * 		The last 2 steps are repeated several times before the statement is <code>close</code>-d
 * </UL
 */
public aspect JdbcPreparedStatementOperationCollectionAspect
        extends OperationCollectionAspectSupport {
    /**
     * A {@link Map} of the prepared statements &quot;templates&quot;
     */
    private final WeakKeyHashMap<PreparedStatement, Operation> storage = new WeakKeyHashMap<PreparedStatement, Operation>();
    /**
     * A {@link Map} of the currently entered statements
     */
    private final Map<PreparedStatement, Operation> entered =
            Collections.synchronizedMap(new WeakHashMap<PreparedStatement, Operation>());

    public JdbcPreparedStatementOperationCollectionAspect() {
        super();
    }

    public pointcut metaDataRetrieval(): execution(* java.sql.Connection.getMetaData());
    public pointcut fetchDatabaseUrl(): execution(* java.sql.DatabaseMetaData.getURL());

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
    after(String sql) returning(PreparedStatement statement): preparedStatementCreation(sql) {
        createOperationForStatement(thisJoinPoint, statement, sql);
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after(PreparedStatement statement, int index, Object parameter) returning
            : preparedStatementSetParameter(statement, index, parameter)
            {
                Operation operation = storage.get(statement);
                if (operation != null) {
                    JdbcOperationFinalizer.addParam(operation, index, parameter);
                }
            }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after(PreparedStatement statement, String parameterName, Object parameter) returning
            : callableStatementSetParameter(statement, parameterName, parameter)
            {
                Operation operation = storage.get(statement);
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
        Operation template = storage.get(thisStatement);
        if (template != null) {
            JdbcOperationFinalizer.finalize(template);

            Operation op = new Operation().cloneFrom(template);
            entered.put(thisStatement, op);

            getCollector().enter(op);
        } else {
            // stmt.execute() called, but stmt was never returned via a prepareStatement().
            // possibly someone wrapping a preparedStatement (delegation)
        }
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after() returning(Object returnValue): execute() {

        PreparedStatement thisStatement = (PreparedStatement) thisJoinPoint.getThis();
        entered.remove(thisStatement);

        getCollector().exitNormal(returnValue);
        // removing the softkey entry here actually appears to *degrade* performance
        // This may be because the entire object, including "storage" is thrown away
        // anyways.
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after() throwing(Throwable exception): execute() {
        PreparedStatement thisStatement = (PreparedStatement) thisJoinPoint.getThis();
        entered.remove(thisStatement);

        getCollector().exitAbnormal(exception);
        // See the note above for exitNormal for why we do not explicitly remove
        // the SoftKeyEntries here
    }

    Operation createOperationForStatement(JoinPoint jp, PreparedStatement statement, String sql) {
        Operation operation = new Operation()
                .type(JdbcOperationExternalResourceAnalyzer.TYPE)
                .sourceCodeLocation(getSourceCodeLocation(jp))
                .label(JdbcOperationFinalizer.createLabel(sql))
                .put("sql", sql);
        storage.put(statement, operation);

        // always return an operation
        try {
            Connection connection = statement.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            operation.putAnyNonEmpty(OperationFields.CONNECTION_URL, metaData.getURL());
        } catch (SQLException e) {
            // ignore, possibly expected
        } catch (Throwable t) {
            CollectionErrors.markCollectionError(this, t);
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
