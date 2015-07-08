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


import com.datastax.driver.core.*;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.intercept.operation.OperationList;
import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.StringUtil;
import org.aspectj.lang.annotation.SuppressAjWarnings;

public aspect CassandraSessionExecuteOperationCollectionAspect extends OperationCollectionAspectSupport {

    public CassandraSessionExecuteOperationCollectionAspect() {
        super();
    }

    public pointcut collect()
        :  if (strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
        ;

    public pointcut executeStatement(Session session, Statement statement)
        : collect()
        && execution(* Session.execute(Statement))
        && this(session)
        && args(statement)
        ;


    @SuppressAjWarnings({"adviceDidNotMatch"})
    before(Session session, Statement statement)
            : executeStatement(session, statement) {

        if ((statement instanceof BoundStatement) ||
            (statement instanceof SimpleStatement)) {
            beforeBoundOrSimpleStatementExecute(thisJoinPoint, session, statement);
        } else if (statement instanceof RegularStatement) {
            beforeRegularStatementExecute(thisJoinPoint, session, (RegularStatement)statement);
        } else if (statement instanceof BatchStatement) {
            beforeBatchStatementExecute(thisJoinPoint, session, (BatchStatement)statement);
        } else {
            beforeStatementExecute(thisJoinPoint, session, statement);
        }
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after(Session session, Statement statement) returning(Object returnValue)
            : executeStatement(session, statement) {

        if ((statement instanceof BoundStatement) ||
                (statement instanceof SimpleStatement)) {
            CassandraOperationFinalizer.remove(statement);
        }

        getCollector().exitNormal(returnValue);

    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after(Session session, Statement statement) throwing(Throwable exception)
            : executeStatement(session, statement) {

        if ((statement instanceof BoundStatement) ||
                (statement instanceof SimpleStatement)) {
            CassandraOperationFinalizer.remove(statement);
        }

        getCollector().exitAbnormal(exception);
    }

    @Override
    public String getPluginName() {
        return CassandraRuntimePluginDescriptor.PLUGIN_NAME;
    }

    @Override
    public boolean isMetricsGenerator() {
        return true; // This provides an external resource
    }

    private Operation createOperation(JoinPoint jp, Session session, String cql, Object[] arguments) {

        if (StringUtil.isEmpty(cql))
            cql = CassandraOperationFinalizer.UNKNOWN_CQL;

        Operation operation = new Operation()
                .type(CassandraExternalResourceAnalyzer.TYPE)
                .sourceCodeLocation(getSourceCodeLocation(jp))
                .label(CassandraOperationFinalizer.createLabel(cql))
                .put("cql", cql);

        if (arguments != null) {
            int index = 0;
            for (Object arg : arguments) {
                CassandraOperationFinalizer.addParam(operation,index,arg);
                index++;
            }
        }

        return operation;
    }

    private void beforeBoundOrSimpleStatementExecute(JoinPoint jp, Session session, Statement statement) {

        Operation operation = CassandraOperationFinalizer.get(statement);
        if (operation == null) {
            // Handle Bound or Simple statement with no arguments ..
            // as these are not woven by *StatementOperationCollectionAspects
            String cql = null;
            if (statement instanceof SimpleStatement) {
                cql = ((SimpleStatement) statement).getQueryString();
            } else if (statement instanceof BoundStatement)
                cql = ((BoundStatement)statement).preparedStatement().getQueryString();
            operation = createOperation(jp, session, cql, null);

        }

        addClusterInfo(operation, session, statement);

        getCollector().enter(operation);

    }

    private void beforeRegularStatementExecute(JoinPoint jp, Session session, RegularStatement statement) {

        // SimpleStatements are handled above
        // This handles BuiltStatements or anything derived from RegularStatement
        Operation operation = createOperation(jp, session, statement.getQueryString(), null);
        addClusterInfo(operation, session, statement);
        getCollector().enter(operation);
    }

    private void beforeBatchStatementExecute(JoinPoint jp, Session session, BatchStatement statement) {

        //TODO: Extract Statements from batch and expose them in UI
        Operation operation = createOperation(jp, session, CassandraOperationFinalizer.UNKNOWN_CQL + " BATCH ", null);
        addClusterInfo(operation, session, statement);
        getCollector().enter(operation);
    }

    private void beforeStatementExecute(JoinPoint jp, Session session, Statement statement) {

        Operation operation = createOperation(jp, session, CassandraOperationFinalizer.UNKNOWN_CQL + " WRAPPED ", null);
        addClusterInfo(operation, session, statement);
        getCollector().enter(operation);
    }

    private void addClusterInfo(Operation operation, Session session, Statement statement) {

        String keyspace = statement.getKeyspace();
        if (keyspace == null)
            keyspace = session.getLoggedKeyspace();
        operation.putAnyNonEmpty(CassandraOperationFinalizer.KEYSPACE, keyspace);
        Cluster cluster = session.getCluster();
        Metadata metadata = cluster.getMetadata();
        operation.putAnyNonEmpty(CassandraOperationFinalizer.CLUSTER_NAME, metadata.getClusterName());

        operation.put(CassandraOperationFinalizer.PORT, cluster.getConfiguration().getProtocolOptions().getPort());
        addAllHosts(operation, metadata);
    }
    private void addAllHosts(Operation operation, Metadata metadata) {

        OperationList list = operation.createList(CassandraOperationFinalizer.HOSTS);
        for (Host host : metadata.getAllHosts()) {
            list.add(host.getAddress().getHostAddress());
        }
    }
}
