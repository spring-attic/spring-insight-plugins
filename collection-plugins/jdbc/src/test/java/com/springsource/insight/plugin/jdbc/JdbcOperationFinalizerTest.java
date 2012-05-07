/**
 * Copyright 2009-2011 the original author or authors.
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

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.springsource.insight.intercept.operation.Operation;

public class JdbcOperationFinalizerTest {
    
    private Operation operation;
    
    @Before
    public void setup() {
        operation = new Operation().type(JdbcOperationExternalResourceAnalyzer.TYPE);
        JdbcOperationFinalizer.register(operation);
    }
    
    @Test
    public void setParameterInSequence() {
        JdbcOperationFinalizer.addParam(operation, 1, "a"); // this is 1-based index
        JdbcOperationFinalizer.addParam(operation, 2, "b"); // this is 1-based index
        
        operation.finalizeConstruction();
        List<String> params = (List<String>) operation.asMap().get("params");
        
        assertEquals(2, params.size());
        assertEquals("a", params.get(0));
        assertEquals("b", params.get(1));
    }

    @Test
    public void setParameterOutOfSequence() {
        JdbcOperationFinalizer.addParam(operation, 2, "b"); // this is 1-based index
        JdbcOperationFinalizer.addParam(operation, 1, "a"); // this is 1-based index
        
        operation.finalizeConstruction();
        List<String> params = (List<String>) operation.asMap().get("params");
        
        assertEquals(2, params.size());
        assertEquals("a", params.get(0));
        assertEquals("b", params.get(1));
    }

    @Test
    public void setMappedParameter() {
        JdbcOperationFinalizer.addParam(operation, "key1", "value1");
        JdbcOperationFinalizer.addParam(operation, "key2", "value2");
        
        operation.finalizeConstruction();
        Map<String,String> params = (Map<String,String>) operation.asMap().get("params");
        
        assertEquals(2, params.size());
        assertEquals("value1", params.get("key1"));
        assertEquals("value2", params.get("key2"));
    }
        
    @Test
    public void createLabel_select() {
        assertEquals("JDBC SELECT (TABLE)", JdbcOperationFinalizer.createLabel("SELECT * FROM table"));
        assertEquals("JDBC SELECT (TABLE)", JdbcOperationFinalizer.createLabel("SELECT * FROM table t"));
        assertEquals("JDBC SELECT (TABLE)", JdbcOperationFinalizer.createLabel("SELECT * FROM table "));
        assertEquals("JDBC SELECT", JdbcOperationFinalizer.createLabel("SELECT * FROM"));
        assertEquals("JDBC SELECT", JdbcOperationFinalizer.createLabel("SELECT * FROM "));
    }
    
    @Test
    public void createLabel_insert() {
        assertEquals("JDBC INSERT (TABLE)", JdbcOperationFinalizer.createLabel("INSERT INTO table VALUES ..."));
        assertEquals("JDBC INSERT (TABLE)", JdbcOperationFinalizer.createLabel("INSERT INTO table"));
        assertEquals("JDBC INSERT (TABLE)", JdbcOperationFinalizer.createLabel("INSERT INTO table "));
        assertEquals("JDBC INSERT", JdbcOperationFinalizer.createLabel("INSERT INTO"));
        assertEquals("JDBC INSERT", JdbcOperationFinalizer.createLabel("INSERT INTO "));
    }
    
    @Test
    public void createLabel_delete() {
        assertEquals("JDBC DELETE (TABLE)", JdbcOperationFinalizer.createLabel("DELETE FROM table WHERE ..."));
        assertEquals("JDBC DELETE (TABLE)", JdbcOperationFinalizer.createLabel("DELETE FROM table"));
        assertEquals("JDBC DELETE (TABLE)", JdbcOperationFinalizer.createLabel("DELETE FROM table "));
        assertEquals("JDBC DELETE", JdbcOperationFinalizer.createLabel("DELETE FROM"));
        assertEquals("JDBC DELETE", JdbcOperationFinalizer.createLabel("DELETE FROM "));
    }
    
    @Test
    public void createLabel_update() {
        assertEquals("JDBC UPDATE (TABLE)", JdbcOperationFinalizer.createLabel("UPDATE table SET ..."));
        assertEquals("JDBC UPDATE (TABLE)", JdbcOperationFinalizer.createLabel("UPDATE table"));
        assertEquals("JDBC UPDATE (TABLE)", JdbcOperationFinalizer.createLabel("UPDATE table "));
        assertEquals("JDBC UPDATE", JdbcOperationFinalizer.createLabel("UPDATE"));
        assertEquals("JDBC UPDATE", JdbcOperationFinalizer.createLabel("UPDATE "));
    }
    
    @Test
    public void createLabel_call() {
        assertEquals("JDBC CALL (FUNC)", JdbcOperationFinalizer.createLabel("CALL func args"));
        assertEquals("JDBC CALL (FUNC)", JdbcOperationFinalizer.createLabel("CALL func"));
        assertEquals("JDBC CALL (FUNC)", JdbcOperationFinalizer.createLabel("CALL func "));
        assertEquals("JDBC CALL", JdbcOperationFinalizer.createLabel("CALL"));
        assertEquals("JDBC CALL", JdbcOperationFinalizer.createLabel("CALL "));
    }
    
    @Test
    public void createLabel_checkpoint() {
        assertEquals("JDBC CHECKPOINT", JdbcOperationFinalizer.createLabel("CHECKPOINT"));
        assertEquals("JDBC CHECKPOINT", JdbcOperationFinalizer.createLabel("CHECKPOINT FUBAR"));
    }
    
    @Test
    public void createLabel_createTable() {
        assertEquals("JDBC CREATE TABLE", JdbcOperationFinalizer.createLabel("CREATE TABLE FUBAR(BAZ)"));
        assertEquals("JDBC CREATE TABLE", JdbcOperationFinalizer.createLabel("CREATE TABLE "));
        assertEquals("JDBC CREATE TABLE", JdbcOperationFinalizer.createLabel("CREATE TABLE"));
    }
    
    @Test
    public void createLabel_createIndex() {
        assertEquals("JDBC CREATE INDEX", JdbcOperationFinalizer.createLabel("CREATE INDEX FUBAR(BAZ)"));
        assertEquals("JDBC CREATE INDEX", JdbcOperationFinalizer.createLabel("CREATE INDEX "));
        assertEquals("JDBC CREATE INDEX", JdbcOperationFinalizer.createLabel("CREATE INDEX"));
        assertEquals("JDBC CREATE INDEX", JdbcOperationFinalizer.createLabel("CREATE UNIQUE INDEX"));
        assertEquals("JDBC CREATE INDEX", JdbcOperationFinalizer.createLabel("CREATE UNIQUE INDEX BARFO"));
    }
    
    @Test
    public void createLabel_dml() {
        assertEquals("JDBC DML", JdbcOperationFinalizer.createLabel("CREATE INMEMORY TABLE FUBAR(BAZ)"));
        assertEquals("JDBC DML", JdbcOperationFinalizer.createLabel("CREATE USER BARFO"));
    }
    
    @Test
    public void createLabel_statement() {
        assertEquals("JDBC STATEMENT", JdbcOperationFinalizer.createLabel("ALTER MY DRESS"));
    }
    
}
