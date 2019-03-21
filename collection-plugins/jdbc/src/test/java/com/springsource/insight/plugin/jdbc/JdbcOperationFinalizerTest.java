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

import org.junit.Test;

import com.springsource.insight.collection.test.AbstractCollectionTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.SourceCodeLocation;

public class JdbcOperationFinalizerTest extends AbstractCollectionTestSupport {
    
    public JdbcOperationFinalizerTest () {
    	super();
    }

    public void testSetParameterInSequence() {
    	Operation	operation=getTestOperation("testSetParameterInSequence");
        JdbcOperationFinalizer.addParam(operation, 1, "a"); // this is 1-based index
        JdbcOperationFinalizer.addParam(operation, 2, "b"); // this is 1-based index
        JdbcOperationFinalizer.finalize(operation);

        OperationList	params=operation.get(JdbcOperationFinalizer.PARAMS_VALUES, OperationList.class);
        assertNotNull("Missing parameters list", params);
        assertEquals("Mismatched number of parameters", 2, params.size());
        assertEquals("Mismatched 1st parameter", "a", params.get(0));
        assertEquals("Mismatched 2nd parameter", "b", params.get(1));
    }

    @Test
    public void testSetParameterOutOfSequence() {
    	Operation	operation=getTestOperation("testSetParameterOutOfSequence");
        JdbcOperationFinalizer.addParam(operation, 2, "b"); // this is 1-based index
        JdbcOperationFinalizer.addParam(operation, 1, "a"); // this is 1-based index
        JdbcOperationFinalizer.finalize(operation);
        
        OperationList	params=operation.get(JdbcOperationFinalizer.PARAMS_VALUES, OperationList.class);
        assertNotNull("Missing parameters list", params);
        assertEquals("Mismatched number of parameters", 2, params.size());
        assertEquals("Mismatched 1st parameter", "a", params.get(0));
        assertEquals("Mismatched 2nd parameter", "b", params.get(1));
    }

    @Test
    public void testSetMappedParameter() {
    	Operation	operation=getTestOperation("testSetMappedParameter");
        JdbcOperationFinalizer.addParam(operation, "key1", "value1");
        JdbcOperationFinalizer.addParam(operation, "key2", "value2");
        JdbcOperationFinalizer.finalize(operation);
        
        OperationMap params = operation.get(JdbcOperationFinalizer.PARAMS_VALUES, OperationMap.class);
        assertNotNull("Missing parameters map", params);
        assertEquals("Mismatched mapped params size", 2, params.size());
        assertEquals("Mismatched 1st key value", "value1", params.get("key1"));
        assertEquals("Mismatched 2nd key value", "value2", params.get("key2"));
    }

    @Test
    public void testMappedParamsValuesClearedBetweenSuccessiveInvocations() {
    	Operation	operation=getTestOperation("testMappedParamsValuesClearedBetweenSuccessiveInvocations");
        JdbcOperationFinalizer.addParam(operation, "key1", "value1");
        JdbcOperationFinalizer.addParam(operation, "key2", "value2");
        JdbcOperationFinalizer.finalize(operation);
    	
        OperationMap params = operation.get(JdbcOperationFinalizer.PARAMS_VALUES, OperationMap.class);
        assertNotNull("Missing parameters map", params);
        
        JdbcOperationFinalizer.finalize(operation);
        assertNullValue("Unexpected parameters map", operation.get(JdbcOperationFinalizer.PARAMS_VALUES));
    }

    @Test
    public void testIndexedParamsValuesClearedBetweenSuccessiveInvocations() {
    	Operation	operation=getTestOperation("testIndexedParamsValuesClearedBetweenSuccessiveInvocations");
        JdbcOperationFinalizer.addParam(operation, 1, "a"); // this is 1-based index
        JdbcOperationFinalizer.addParam(operation, 2, "b"); // this is 1-based index
        JdbcOperationFinalizer.finalize(operation);

        OperationList	params=operation.get(JdbcOperationFinalizer.PARAMS_VALUES, OperationList.class);
        assertNotNull("Missing parameters list", params);
        
        JdbcOperationFinalizer.finalize(operation);
        assertNullValue("Unexpected parameters list", operation.get(JdbcOperationFinalizer.PARAMS_VALUES));
    }

    @Test
    public void testCreateLabelSelect() {
        assertEquals("JDBC SELECT (TABLE)", JdbcOperationFinalizer.createLabel("select * from table"));
        assertEquals("JDBC SELECT (TABLE)", JdbcOperationFinalizer.createLabel("select * from table t"));
        assertEquals("JDBC SELECT (TABLE)", JdbcOperationFinalizer.createLabel("select * from table t where x"));
        assertEquals("JDBC SELECT", JdbcOperationFinalizer.createLabel("SELECT * FROM"));
    }
    
    @Test
    public void testCreateLabelInsert() {
        assertEquals("JDBC INSERT (TABLE)", JdbcOperationFinalizer.createLabel("insert into table values ..."));
        assertEquals("JDBC INSERT (TABLE)", JdbcOperationFinalizer.createLabel("insert into table"));
        assertEquals("JDBC INSERT", JdbcOperationFinalizer.createLabel("INSERT INTO"));
    }
    
    @Test
    public void testCreateLabelDelete() {
        assertEquals("JDBC DELETE (TABLE)", JdbcOperationFinalizer.createLabel("delete from table where ..."));
        assertEquals("JDBC DELETE (TABLE)", JdbcOperationFinalizer.createLabel("delete from table"));
        assertEquals("JDBC DELETE", JdbcOperationFinalizer.createLabel("DELETE FROM"));
    }
    
    @Test
    public void testCreateLabelUpdate() {
        assertEquals("JDBC UPDATE (TABLE)", JdbcOperationFinalizer.createLabel("update table set ..."));
        assertEquals("JDBC UPDATE (TABLE)", JdbcOperationFinalizer.createLabel("update table"));
        assertEquals("JDBC UPDATE", JdbcOperationFinalizer.createLabel("UPDATE"));
    }
    
    @Test
    public void testCreateLabelCall() {
        assertEquals("JDBC CALL (FUNC)", JdbcOperationFinalizer.createLabel("CALL func args"));
        assertEquals("JDBC CALL (FUNC)", JdbcOperationFinalizer.createLabel("CALL func"));
        assertEquals("JDBC CALL", JdbcOperationFinalizer.createLabel("CALL"));
    }
    
    @Test
    public void testCreateLabelCheckpoint() {
        assertEquals("JDBC CHECKPOINT", JdbcOperationFinalizer.createLabel("CHECKPOINT"));
        assertEquals("JDBC CHECKPOINT", JdbcOperationFinalizer.createLabel("CHECKPOINT FUBAR"));
    }
    
    @Test
    public void testCreateLabelCreateTable() {
        assertEquals("JDBC CREATE TABLE (FUBAR)", JdbcOperationFinalizer.createLabel("CREATE TABLE FUBAR(BAZ)"));
        assertEquals("JDBC CREATE TABLE", JdbcOperationFinalizer.createLabel("CREATE TABLE"));
    }

    @Test
    public void testCreateLabelAlterTable() {
        assertEquals("JDBC ALTER TABLE (FUBAR)", JdbcOperationFinalizer.createLabel("alter table fubar modify column x"));
        assertEquals("JDBC ALTER TABLE", JdbcOperationFinalizer.createLabel("ALTER TABLE"));
    }

    @Test
    public void testCreateLabelDropTable() {
        assertEquals("JDBC DROP TABLE (FUBAR)", JdbcOperationFinalizer.createLabel("drop table fubar purge erase"));
        assertEquals("JDBC DROP TABLE", JdbcOperationFinalizer.createLabel("drop table"));
    }

    @Test
    public void testCreateLabelCreateIndex() {
        assertEquals("JDBC CREATE INDEX (FUBAR)", JdbcOperationFinalizer.createLabel("create index fubar(baz)"));
        assertEquals("JDBC CREATE INDEX", JdbcOperationFinalizer.createLabel("CREATE INDEX"));
        assertEquals("JDBC CREATE UNIQUE INDEX (BARFO)", JdbcOperationFinalizer.createLabel("create unique index barfo(blah)"));
        assertEquals("JDBC CREATE UNIQUE INDEX", JdbcOperationFinalizer.createLabel("CREATE UNIQUE INDEX"));
    }
    
    @Test
    public void testCreateLabelDml() {
        assertEquals("JDBC DML", JdbcOperationFinalizer.createLabel("CREATE INMEMORY TABLE FUBAR(BAZ)"));
        assertEquals("JDBC DML", JdbcOperationFinalizer.createLabel("CREATE USER BARFO"));
    }
    
    @Test
    public void testCreateLabelStatement() {
        assertEquals("JDBC STATEMENT", JdbcOperationFinalizer.createLabel("ALTER MY DRESS"));
    }
    
    private Operation getTestOperation(String methodName) {
    	return new Operation()
    					.type(JdbcOperationExternalResourceAnalyzer.TYPE)
    					.label(methodName)
    					.sourceCodeLocation(new SourceCodeLocation(getClass().getName(), methodName, -1))
    					;
    }
}
