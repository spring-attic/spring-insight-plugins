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

import static com.springsource.insight.util.StringUtil.indexOfNotIn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFinalizer;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.util.StringFormatterUtils;

public class JdbcOperationFinalizer implements OperationFinalizer {

    private static final JdbcOperationFinalizer INSTANCE = new JdbcOperationFinalizer();
    

    /**
     * The keys in these maps should be strongly referenced in the frame stack; so they should not
     * be removed until the frame leaves the station.
     */
    private static final WeakKeyHashMap<Operation, Map<String, Object>> mappedParamStorage = new WeakKeyHashMap<Operation, Map<String, Object>>();
    private static final WeakKeyHashMap<Operation, List<Object>> indexedParamStorage = new WeakKeyHashMap<Operation, List<Object>>();

    public static void register(Operation operation) {
        operation.addFinalizer(INSTANCE);
    }
    
    public static void addParam(Operation operation, String key, Object param) {
        synchronized (operation) {
            Map<String, Object> params = mappedParamStorage.get(operation);
            if (params == null) {
                params = new HashMap<String, Object>();
                mappedParamStorage.put(operation, params);
            }
            params.put(key, param);
        }
    }
    
    public static void addParam(Operation operation, int paramIndex, Object param) {
        // JDBC indexes are 1-based, so let's adjust it to the modern world first!
        int index=paramIndex - 1;
        synchronized (operation) {
            List<Object> params = indexedParamStorage.get(operation);
            if (params == null) {
                params = new ArrayList<Object>();
                indexedParamStorage.put(operation, params);
            }
            // grow array if needed
            while (index >= params.size()) {
                params.add(null);
            }
            params.set(index, param);
        }
    }
    
    public void finalize(Operation operation, Map<String, Object> richObjects) {
        operation.label(createLabel(operation.get("sql", String.class)));
        if (mappedParamStorage.get(operation) != null) {
            OperationMap params = operation.createMap("params");
            for (Entry<String, Object> entry : mappedParamStorage.get(operation).entrySet()) {
                params.put(entry.getKey(), StringFormatterUtils.formatObjectAndTrim(entry.getValue()));
            }
        }
        else if (indexedParamStorage.get(operation) != null) {
            OperationList params = operation.createList("params");
            for (Object param : indexedParamStorage.get(operation)) {
                params.add(StringFormatterUtils.formatObjectAndTrim(param));
            }
        }
        /**
         * We know we will never need these SoftKeyEntries again, so let's
         * remove it explicitly so they are cleaned up using traditional
         * garbage collection.
         */
        mappedParamStorage.remove(operation);
        indexedParamStorage.remove(operation);
    }
    
    public static String createLabel(String sql) {
        if (sql == null) {
            return "JDBC";
        }
        String upperSql = sql.toUpperCase();

        if (upperSql.startsWith("SELECT")) {
            String tableName = captureWordAfter(upperSql, " FROM ");
            if (tableName != null) {
                return "JDBC SELECT (" + tableName + ")";    
            } 
            return "JDBC SELECT";
        } else if (upperSql.startsWith("INSERT")) {
            String tableName = captureWordAfter(upperSql, " INTO ");
            if (tableName != null) {
                return "JDBC INSERT (" + tableName + ")";
            }
            return "JDBC INSERT";
        } else if (upperSql.startsWith("DELETE")) {
            String tableName = captureWordAfter(upperSql, " FROM ");
            if (tableName != null) {
                return "JDBC DELETE (" + tableName + ")";
            }
            return "JDBC DELETE";
        } else if (upperSql.startsWith("UPDATE")) {
            String tableName = captureWordAfter(upperSql, "UPDATE ");
            if (tableName != null) {
                return "JDBC UPDATE (" + tableName + ")";
            }
            return "JDBC UPDATE";
        } else if (upperSql.startsWith("CREATE TABLE")) {
            return "JDBC CREATE TABLE";
        } else if (upperSql.startsWith("CREATE INDEX") ||
                   upperSql.startsWith("CREATE UNIQUE INDEX")) 
        {
            return "JDBC CREATE INDEX";
        } else if (upperSql.startsWith("CREATE")) {
            return "JDBC DML";
        } else if (upperSql.startsWith("CHECKPOINT")) {
            return "JDBC CHECKPOINT";
        } else if (upperSql.startsWith("CALL")) {
            String procedureName = captureWordAfter(upperSql, "CALL ");
            if (procedureName != null) {
                return "JDBC CALL (" + procedureName + ")";
            }
            return "JDBC CALL";
        } else {
            return "JDBC STATEMENT"; // could be any number of unhandled JDBC statements
        }
    }

    private static final Set<Character> WORD_DELIMS=
        Collections.unmodifiableSet(new HashSet<Character>(Arrays.asList(Character.valueOf(' '))));
    private static String captureWordAfter(String source, String delim) {
        if (delim.charAt(delim.length() - 1) != ' ') {
            throw new IllegalArgumentException("Last char must be a ' '");
        }
        int fromIdx = source.indexOf(delim);
        if (fromIdx == -1) {
            return null;
        }

        String strAfterDelim = source.substring(fromIdx + delim.length() - 1);
        int wordIdx = indexOfNotIn(strAfterDelim, WORD_DELIMS);
        if (wordIdx == -1) {
            return null;
        }
        
        int wordEndIdx = strAfterDelim.indexOf(' ', wordIdx);
        if (wordEndIdx == -1) {
            return strAfterDelim.substring(wordIdx);
        } else {
            return strAfterDelim.substring(wordIdx, wordEndIdx);
        }
    }

    
}
