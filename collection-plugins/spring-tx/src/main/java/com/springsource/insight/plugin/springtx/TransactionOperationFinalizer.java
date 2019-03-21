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

package com.springsource.insight.plugin.springtx;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFinalizer;
import com.springsource.insight.util.StringUtil;

public class TransactionOperationFinalizer implements OperationFinalizer {

    private static final TransactionOperationFinalizer INSTANCE = new TransactionOperationFinalizer();
    
    static final int MAX_TX_NAME_LEN = 30;
    
    static final List<String> propagationNames=
            Collections.unmodifiableList(
                    Arrays.asList("REQUIRED", "SUPPORTS", "MANDATORY",
                                  "REQUIRES_NEW", "NOT_SUPPORTED", "NEVER",
                                  "NESTED"));

    static final String DEFAULT_ISOLATION_LEVEL="DEFAULT";
    static final Map<Integer,String>    isolationLevels=
            Collections.unmodifiableMap(new HashMap<Integer,String>() {
                private static final long serialVersionUID = 1L;
                {
                    put(Integer.valueOf(Connection.TRANSACTION_NONE), DEFAULT_ISOLATION_LEVEL);
                    put(Integer.valueOf(Connection.TRANSACTION_READ_COMMITTED), "READ_COMMITTED");
                    put(Integer.valueOf(Connection.TRANSACTION_READ_UNCOMMITTED), "READ_UNCOMMITTED");
                    put(Integer.valueOf(Connection.TRANSACTION_REPEATABLE_READ), "REPEATABLE_READ");
                    put(Integer.valueOf(Connection.TRANSACTION_SERIALIZABLE), "SERIALIZABLE");
                }
            });
    public static void register(Operation operation) {
        operation.addFinalizer(INSTANCE);
    }
    
    // convert values to strings
    public void finalize(Operation operation, Map<String, Object> richObjects) {
        String  level=normalizeIsolation(operation);
        if (level != null) {
            operation.put("isolation", level);
        }

        String  propagation=normalizePropagation(operation);
        if (propagation != null) {
            operation.put("propagation", propagation);
        }

        operation.label(buildLabel(operation));
    }
    
    static String normalizeIsolation(Operation operation) {
        int 	isolation=operation.getInt("isolation", (-1));
        if (isolation < 0) {
        	return DEFAULT_ISOLATION_LEVEL;	// debug breakpoint
        }

        String  level=isolationLevels.get(Integer.valueOf(isolation));
        if (StringUtil.isEmpty(level)) {
            return DEFAULT_ISOLATION_LEVEL;
        } else {
        	return level;
        }
    }
    
    static String normalizePropagation(Operation operation) {
        int propagation=operation.getInt("propagation", (-1));
        if ((propagation >= 0) && (propagation < propagationNames.size())) {
            return propagationNames.get(propagation);
        } else {
        	return null;
        }
    }
    
    static String buildLabel(Operation operation) {
        StringBuilder label=new StringBuilder("Transaction");
        String        name=operation.get("name", String.class);
        if (name != null) {
            label.append(": ")
                 .append(truncateTxName(name, MAX_TX_NAME_LEN));
        }

        Boolean readOnly=operation.get("readOnly", Boolean.class, Boolean.FALSE);
        if (readOnly.booleanValue()) {
            label.append(" (Read-only)");
        }
        else if (TransactionOperationStatus.RolledBack.toString().equals(operation.get("status", String.class))) {
            label.append(" (Rolled Back)");
        }

        return label.toString();
    }
    
    static String truncateTxName(String txName, int maxChars) {
        int packageIdx = StringUtil.indexOfNthCharFromTail(txName, '.', 2);
        if (packageIdx == -1) {
            // Not a Spring formatted transaction name.  This can apparently happen with WebLogic
            return StringUtil.chopTailAndEllipsify(txName, maxChars);
        }
        String chopped = txName.substring(packageIdx + 1);
        return StringUtil.chopTailAndEllipsify(chopped, maxChars);
    }
}
