/**
 * Copyright 2009-2010 the original author or authors.
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

package com.springsource.insight.plugin.springtx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.junit.Test;
import org.springframework.transaction.TransactionDefinition;

import com.springsource.insight.intercept.operation.Operation;


public class TransactionOperationFinalizerTest {
    @Test
    public void getVariousStrings() {
        Operation testTO1 = new Operation()
            .put("propagation", TransactionDefinition.PROPAGATION_REQUIRED)
            .put("isolation", TransactionDefinition.ISOLATION_READ_COMMITTED);
        finalize(testTO1);
        assertEquals("REQUIRED", testTO1.get("propagation"));
        assertEquals("READ_COMMITTED", testTO1.get("isolation"));

        Operation testTO2 = new Operation()
            .put("propagation", TransactionDefinition.PROPAGATION_REQUIRES_NEW)
            .put("isolation", TransactionDefinition.ISOLATION_DEFAULT);
        finalize(testTO2);
        assertEquals("REQUIRES_NEW", testTO2.get("propagation"));
        assertEquals("DEFAULT", testTO2.get("isolation"));
    }
    
    @Test
    public void getLabel() {
        Operation op = new Operation().put("name", "myTx");
        finalize(op);
        assertEquals("Transaction: myTx", op.getLabel());
    }
    
    @Test
    public void getLabel_truncated() {
        Operation op = new Operation().put("name", "0123456789012345678901.34567890");
        finalize(op);
        assertEquals("Transaction: 0123456789012345678901.3456...", op.getLabel());
    }
    
    @Test
    public void getLabel_withNoTxName() {
        Operation op = new Operation();
        finalize(op);
        assertEquals("Transaction", op.getLabel());
    }
    
    @Test
    public void getLabel_readOnly() {
        Operation op = new Operation().put("name", "myTx").put("readOnly", true);
        finalize(op);
        assertEquals("Transaction: myTx (Read-only)", op.getLabel());
    }
    
    @Test
    public void getLabel_rolledBack() {
        Operation op = new Operation().put("name", "myTx").put("status", TransactionOperationStatus.RolledBack.toString());
        finalize(op);
        assertEquals("Transaction: myTx (Rolled Back)", op.getLabel());
    }
    
    @Test
    public void truncateTxName() {
        assertEquals("", TransactionOperationFinalizer.truncateTxName("", 5));
        assertEquals("MyTx", TransactionOperationFinalizer.truncateTxName("MyTx", 5));        
        assertEquals("MyTra", TransactionOperationFinalizer.truncateTxName("MyTra", 5));
        assertEquals("My...", TransactionOperationFinalizer.truncateTxName("MyTrans", 5));
        
        assertEquals(".", TransactionOperationFinalizer.truncateTxName("..", 5));
        assertEquals(".", TransactionOperationFinalizer.truncateTxName(".", 5));
        assertEquals("Foo.Meth...", TransactionOperationFinalizer.truncateTxName("com.my.Foo.MethodName", 11));
        assertEquals("Foo.Meth...", TransactionOperationFinalizer.truncateTxName("Foo.MethodName", 11));
        assertEquals("Foo.Meth...", TransactionOperationFinalizer.truncateTxName(".Foo.MethodName", 11));                                
    }

    @Test
    public void normalizePropagation () {
        for (int index=0; index < TransactionOperationFinalizer.propagationNames.size(); index++) {
            String  expected=TransactionOperationFinalizer.propagationNames.get(index),
                    actual=TransactionOperationFinalizer.normalizePropagation(new Operation().put("propagation", index));
            assertEquals("Mismatched values for index=" + index, expected, actual);
        }
    }

    @Test
    public void normalizeBadPropagationValues () {
        assertNull("Unexpected result for null",
                   TransactionOperationFinalizer.normalizePropagation(new Operation()));
        assertNull("Unexpected result for negative index",
                   TransactionOperationFinalizer.normalizePropagation(new Operation().put("propagation", (-1))));
        assertNull("Unexpected result for out-of-bounds index",
                   TransactionOperationFinalizer.normalizePropagation(
                        new Operation().put("propagation", TransactionOperationFinalizer.propagationNames.size() + 1)));
    }

    @Test
    public void normalizeIsolation () {
        for (Map.Entry<Integer,String> ie : TransactionOperationFinalizer.isolationLevels.entrySet()) {
            Integer value=ie.getKey();
            String  expected=ie.getValue(),
                    actual=TransactionOperationFinalizer.normalizeIsolation(new Operation().put("isolation", value.intValue()));
            assertEquals("Mismatched values for value=" + value, expected, actual);
        }
    }

    @Test
    public void normalizeBadIsolationValues () {
        assertNull("Unexpected result for null", TransactionOperationFinalizer.normalizeIsolation(new Operation()));
        assertEquals("Unexpected negative value result",
                     TransactionOperationFinalizer.DEFAULT_ISOLATION_LEVEL,
                     TransactionOperationFinalizer.normalizeIsolation(new Operation().put("isolation", (-1))));

        int maxValue=Integer.MIN_VALUE;
        for (Integer value : TransactionOperationFinalizer.isolationLevels.keySet()) {
            if (maxValue < value.intValue()) {
                maxValue = value.intValue();
            }
        }

        assertEquals("Unexpected beyond max. value result",
                TransactionOperationFinalizer.DEFAULT_ISOLATION_LEVEL,
                TransactionOperationFinalizer.normalizeIsolation(new Operation().put("isolation", maxValue + 1)));
    }

    private static void finalize(Operation operation) {
        TransactionOperationFinalizer.register(operation);
        operation.finalizeConstruction();
    }
    
}
