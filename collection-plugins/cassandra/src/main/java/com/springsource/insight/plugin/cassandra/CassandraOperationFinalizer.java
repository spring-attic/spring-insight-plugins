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

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Statement;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringFormatterUtils;
import com.springsource.insight.util.StringUtil;

public class CassandraOperationFinalizer {

    public static final String KEYSPACE = "keyspace";
    public static final String CLUSTER_NAME ="clustername";
    public static final String PORT = "cassandra_port";
    public static final String HOSTS = "cassandra_hosts";
    public static final String PARAMS_VALUES = "params";

    public static final String UNKNOWN_CQL = "UNKNOWN";

    static final WeakKeyHashMap<Statement, Operation> storage = new WeakKeyHashMap<Statement, Operation>();

    private CassandraOperationFinalizer() {
        throw new UnsupportedOperationException("No instance");
    }

    public static Operation get(Statement statement) {
        return storage.get(statement);
    }
    public static Operation put(Statement statement, Operation operation) {
        return storage.put(statement, operation);
    }
    public static Operation remove(Statement statement) {
        return storage.remove(statement);
    }

    public static void addParam(Operation operation, String key, Object param) {

        OperationMap params = operation.get(PARAMS_VALUES, OperationMap.class);
        if (params == null)
            params = operation.createMap(PARAMS_VALUES);
        params.put(key, StringFormatterUtils.formatObjectAndTrim(param));
    }

    public static void addParam(Operation operation, int index, Object param) {
        addParam(operation, "P_" + index, param);
    }

    private static class Keywords {

        private String keyword;
        private String argPos;

        public Keywords(String keyword, String argPos) {
            this.argPos = argPos;
            this.keyword = keyword;
        }

        public String getKeyword() {
            return keyword;
        }

        public String getArgPos() {
            return argPos;
        }
    }

    private static final Collection<Keywords> stmtsList =
            Collections.unmodifiableList(new ArrayList<Keywords>() {
                {
                    add(new Keywords("SELECT", " FROM "));
                    add(new Keywords("INSERT", " INTO "));
                    add(new Keywords("DELETE", " FROM "));
                    add(new Keywords("UPDATE", "UPDATE "));
                    add(new Keywords("CREATE TABLE IF NOT EXISTS", " EXISTS "));
                    add(new Keywords("CREATE TABLE", " TABLE "));
                    add(new Keywords("CREATE KEYSPACE IF NOT EXISTS", " EXISTS "));
                    add(new Keywords("CREATE KEYSPACE", " KEYSPACE "));
                    add(new Keywords("ALTER TABLE", " TABLE "));
                    add(new Keywords("DROP TABLE IF EXISTS", " EXISTS "));
                    add(new Keywords("DROP TABLE", " TABLE "));
                    add(new Keywords("CREATE INDEX IF NOT EXISTS", " EXISTS "));
                    add(new Keywords("CREATE INDEX", " INDEX "));
                    add(new Keywords("CREATE CUSTOM INDEX IF NOT EXISTS", " EXISTS "));
                    add(new Keywords("CREATE CUSTOM INDEX", " INDEX "));
                    add(new Keywords("DROP INDEX IF EXISTS", " EXISTS "));
                    add(new Keywords("DROP INDEX", " INDEX "));
                    add(new Keywords("TRUNCATE", " TRUNCATE "));
                    add(new Keywords("USE", " USE "));
                }
            });

    public static String createLabel(String cql) {
        if (StringUtil.isEmpty(cql)) {
            return "CQL";
        }

        String upperSql = cql.toUpperCase().trim();
        for (Keywords keywords : stmtsList) {
            String kwd = keywords.getKeyword();
            if (!upperSql.startsWith(kwd)) {
                continue;
            }

            String argPos = keywords.getArgPos();
            return appendArgumentValue("CQL " + kwd, captureWordAfter(upperSql, argPos));
        }

        // some special extra statements 
        if (upperSql.startsWith("CREATE") ||
                upperSql.startsWith("DROP") ||
                upperSql.startsWith("ALTER")
                ) {
            return "CQL DML";
        } else {
            return "CQL STATEMENT"; // could be any number of unhandled CQL statements
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
