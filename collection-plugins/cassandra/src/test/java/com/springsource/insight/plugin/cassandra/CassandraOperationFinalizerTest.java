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


import com.springsource.insight.collection.test.AbstractCollectionTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.SourceCodeLocation;
import org.junit.Test;

public class CassandraOperationFinalizerTest extends AbstractCollectionTestSupport {

    @Test
    public void testSetParameterInSequence() {
        Operation operation = getTestOperation("testSetParameterInSequence");
        CassandraOperationFinalizer.addParam(operation, 1, "a"); // this is 1-based index
        CassandraOperationFinalizer.addParam(operation, 2, "b"); // this is 1-based index

        OperationMap params = operation.get(CassandraOperationFinalizer.PARAMS_VALUES, OperationMap.class);
        assertNotNull("Missing parameters", params);
        assertEquals("Mismatched number of parameters", 2, params.size());
        assertEquals("Mismatched 1st parameter", "a", params.get("P_1"));
        assertEquals("Mismatched 2nd parameter", "b", params.get("P_2"));
    }
    @Test
    public void testSetParameterOutOfSequence() {
        Operation operation = getTestOperation("testSetParameterOutOfSequence");
        CassandraOperationFinalizer.addParam(operation, 2, "b"); // this is 1-based index
        CassandraOperationFinalizer.addParam(operation, 1, "a"); // this is 1-based index

        OperationMap params = operation.get(CassandraOperationFinalizer.PARAMS_VALUES, OperationMap.class);
        assertNotNull("Missing parameters list", params);
        assertEquals("Mismatched number of parameters", 2, params.size());
        assertEquals("Mismatched 1st parameter", "a", params.get("P_1"));
        assertEquals("Mismatched 2nd parameter", "b", params.get("P_2"));
    }
    @Test
    public void testSetMappedParameter() {
        Operation operation = getTestOperation("testSetMappedParameter");
        CassandraOperationFinalizer.addParam(operation, "key1", "value1");
        CassandraOperationFinalizer.addParam(operation, "key2", "value2");

        OperationMap params = operation.get(CassandraOperationFinalizer.PARAMS_VALUES, OperationMap.class);
        assertNotNull("Missing parameters map", params);
        assertEquals("Mismatched mapped params size", 2, params.size());
        assertEquals("Mismatched 1st key value", "value1", params.get("key1"));
        assertEquals("Mismatched 2nd key value", "value2", params.get("key2"));
    }

    @Test
    public void testCreateLabelSelect() {
        assertEquals("CQL SELECT (TABLE)", CassandraOperationFinalizer.createLabel("select * from table"));
        assertEquals("CQL SELECT (TABLE)", CassandraOperationFinalizer.createLabel("select * from table t"));
        assertEquals("CQL SELECT (TABLE)", CassandraOperationFinalizer.createLabel("select * from table t where x"));
        assertEquals("CQL SELECT", CassandraOperationFinalizer.createLabel("SELECT * FROM"));
    }
    @Test
    public void testCreateLabelDelete() {
        assertEquals("CQL DELETE (TABLE)", CassandraOperationFinalizer.createLabel("delete from table where ..."));
        assertEquals("CQL DELETE (TABLE)", CassandraOperationFinalizer.createLabel("delete from table"));
        assertEquals("CQL DELETE", CassandraOperationFinalizer.createLabel("DELETE FROM"));
    }

    @Test
    public void testCreateLabelUpdate() {
        assertEquals("CQL UPDATE (TABLE)", CassandraOperationFinalizer.createLabel("update table set ..."));
        assertEquals("CQL UPDATE (TABLE)", CassandraOperationFinalizer.createLabel("update table"));
        assertEquals("CQL UPDATE", CassandraOperationFinalizer.createLabel("UPDATE"));
    }

    @Test
    public void testCreateLabelCreateTable() {
        assertEquals("CQL CREATE TABLE (FUBAR)", CassandraOperationFinalizer.createLabel("CREATE TABLE FUBAR(BAZ)"));
        assertEquals("CQL CREATE TABLE", CassandraOperationFinalizer.createLabel("CREATE TABLE"));
        assertEquals("CQL CREATE TABLE IF NOT EXISTS (FUBAR)", CassandraOperationFinalizer.createLabel("CREATE TABLE IF NOT EXISTS FUBAR(BAZ)"));
    }

    @Test
    public void testCreateLabelCreateKeyspace() {
        assertEquals("CQL CREATE KEYSPACE (FUBAR)", CassandraOperationFinalizer.createLabel("CREATE KEYSPACE FUBAR WITH REPLICATION"));
        assertEquals("CQL CREATE KEYSPACE", CassandraOperationFinalizer.createLabel("CREATE KEYSPACE"));
        assertEquals("CQL CREATE KEYSPACE IF NOT EXISTS (FUBAR)", CassandraOperationFinalizer.createLabel("CREATE KEYSPACE IF NOT EXISTS FUBAR"));
    }


    @Test
    public void testCreateLabelAlterTable() {
        assertEquals("CQL ALTER TABLE (FUBAR)", CassandraOperationFinalizer.createLabel("alter table fubar modify column x"));
        assertEquals("CQL ALTER TABLE", CassandraOperationFinalizer.createLabel("ALTER TABLE"));
    }

    @Test
    public void testCreateLabelDropTable() {
        assertEquals("CQL DROP TABLE (FUBAR)", CassandraOperationFinalizer.createLabel("drop table fubar"));
        assertEquals("CQL DROP TABLE IF EXISTS (FUBAR)", CassandraOperationFinalizer.createLabel("drop table if exists fubar"));
        assertEquals("CQL DROP TABLE", CassandraOperationFinalizer.createLabel("drop table"));
    }

    @Test
    public void testCreateLabelDropIndex() {
        assertEquals("CQL DROP INDEX (FUBAR)", CassandraOperationFinalizer.createLabel("drop index fubar"));
        assertEquals("CQL DROP INDEX IF EXISTS (FUBAR)", CassandraOperationFinalizer.createLabel("drop index if exists fubar"));
        assertEquals("CQL DROP INDEX", CassandraOperationFinalizer.createLabel("drop index"));
    }


    @Test
    public void testCreateLabelCreateIndex() {
        assertEquals("CQL CREATE INDEX (FUBAR)", CassandraOperationFinalizer.createLabel("create index fubar"));
        assertEquals("CQL CREATE INDEX IF NOT EXISTS (FUBAR)", CassandraOperationFinalizer.createLabel("create index if not exists fubar"));
        assertEquals("CQL CREATE INDEX", CassandraOperationFinalizer.createLabel("CREATE INDEX"));
        assertEquals("CQL CREATE CUSTOM INDEX (BARFO)", CassandraOperationFinalizer.createLabel("create custom index barfo"));
        assertEquals("CQL CREATE CUSTOM INDEX", CassandraOperationFinalizer.createLabel("CREATE CUSTOM INDEX"));
    }

    @Test
    public void testCreateLabelDml() {
        assertEquals("CQL DML", CassandraOperationFinalizer.createLabel("CREATE TYPE keyspace.type_name"));
        assertEquals("CQL DML", CassandraOperationFinalizer.createLabel("CREATE USER BARFO"));
    }

    @Test
    public void testCreateLabelStatement() {
        assertEquals("CQL STATEMENT", CassandraOperationFinalizer.createLabel("BEGIN UNLOGGED BATCH INSERT x APPLY BATCH;"));
    }

    private Operation getTestOperation(String methodName) {
        return new Operation()
                .type(CassandraExternalResourceAnalyzer.TYPE)
                .label(methodName)
                .sourceCodeLocation(new SourceCodeLocation(getClass().getName(), methodName, -1))
                ;
    }
}
