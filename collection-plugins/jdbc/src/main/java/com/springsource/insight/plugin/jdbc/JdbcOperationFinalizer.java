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

import static com.springsource.insight.util.StringUtil.indexOfNotIn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringFormatterUtils;
import com.springsource.insight.util.StringUtil;

public class JdbcOperationFinalizer {
    public static final String PARAMS_VALUES = "params";

    /**
     * The keys in these maps should be strongly referenced in the frame stack; so they should not
     * be removed until the frame leaves the station.
     */
    private static final WeakKeyHashMap<Operation, Map<String, Object>> mappedParamStorage = new WeakKeyHashMap<Operation, Map<String, Object>>();
    private static final WeakKeyHashMap<Operation, List<Object>> indexedParamStorage = new WeakKeyHashMap<Operation, List<Object>>();

    private JdbcOperationFinalizer() {
        throw new UnsupportedOperationException("No instance");
    }

    public static void addParam(Operation operation, String key, Object param) {
        synchronized (mappedParamStorage) {
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
        int index = paramIndex - 1;
        synchronized (indexedParamStorage) {
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

    public static Operation finalize(Operation operation) {
        Map<String, Object> mappedValues;
        synchronized (mappedParamStorage) {
            mappedValues = mappedParamStorage.remove(operation);
        }

        List<Object> indexedValues;
        synchronized (indexedParamStorage) {
            indexedValues = indexedParamStorage.remove(operation);
        }

        // make sure we start with a clean slate
        Serializable prev = operation.remove(PARAMS_VALUES);
        if (prev != null) {
            prev = null;    // debug breakpoint
        }

        if (mappedValues != null) {
            OperationMap params = operation.createMap(PARAMS_VALUES);
            for (Entry<String, Object> entry : mappedValues.entrySet()) {
                params.put(entry.getKey(), StringFormatterUtils.formatObjectAndTrim(entry.getValue()));
            }
        } else if (indexedValues != null) {
            OperationList params = operation.createList(PARAMS_VALUES);
            for (Object param : indexedValues) {
                params.add(StringFormatterUtils.formatObjectAndTrim(param));
            }
        }

        return operation;
    }

    private static final Collection<Map.Entry<String, String>> stmtsList =
            Collections.unmodifiableMap(new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER) {
                private static final long serialVersionUID = 1L;

                {
                    put("SELECT", " FROM ");
                    put("INSERT", " INTO ");
                    put("DELETE", " FROM ");
                    put("UPDATE", "UPDATE ");
                    put("CREATE TABLE", " TABLE ");
                    put("ALTER TABLE", " TABLE ");
                    put("DROP TABLE", " TABLE ");
                    put("CREATE INDEX", " INDEX ");
                    put("CREATE UNIQUE INDEX", " INDEX ");
                    put("DROP INDEX", " INDEX ");
                    put("CALL", "CALL ");
                }
            }).entrySet();

    public static String createLabel(String sql) {
        if (StringUtil.isEmpty(sql)) {
            return "JDBC";
        }

        String upperSql = sql.toUpperCase().trim();
        for (Map.Entry<String, String> stmt : stmtsList) {
            String kwd = stmt.getKey();
            if (!upperSql.startsWith(kwd)) {
                continue;
            }

            String argPos = stmt.getValue();
            return appendArgumentValue("JDBC " + kwd, captureWordAfter(upperSql, argPos));
        }

        // some special extra statements 
        if (upperSql.startsWith("CREATE")) {
            return "JDBC DML";
        } else if (upperSql.startsWith("CHECKPOINT")) {
            return "JDBC CHECKPOINT";
        } else {
            return "JDBC STATEMENT"; // could be any number of unhandled JDBC statements
        }
    }

    private static String appendArgumentValue(String prefix, String agrValue) {
        if (StringUtil.isEmpty(agrValue)) {
            return prefix;
        } else {
            return prefix + " (" + agrValue + ")";
        }
    }

    private static final Set<Character> WORD_DELIMS =
            Collections.unmodifiableSet(ListUtil.asSet(Character.valueOf(' '), Character.valueOf('(')));

    private static String captureWordAfter(String source, String delim) {
        if (delim.charAt(delim.length() - 1) != ' ') {
            throw new IllegalArgumentException("Last char must be a ' '");
        }

        int fromIdx = source.indexOf(delim);
        if (fromIdx < 0) {
            return null;
        }

        String strAfterDelim = source.substring(fromIdx + delim.length() - 1).trim();
        int wordIdx = indexOfNotIn(strAfterDelim, WORD_DELIMS);
        if (wordIdx < 0) {
            return null;
        } else if (wordIdx > 0) {
            strAfterDelim = strAfterDelim.substring(wordIdx);
        }

        int wordEndIdx = StringUtil.indexOfIn(strAfterDelim, WORD_DELIMS);
        if (wordEndIdx < 0) {
            return strAfterDelim;
        } else {
            return strAfterDelim.substring(0, wordEndIdx);
        }
    }
}
