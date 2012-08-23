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

package com.springsource.insight.plugin.springtx;

import java.sql.Connection;
import java.util.logging.Logger;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public class TransactionOperationCollectionAspectTest
        extends OperationCollectionAspectTestSupport {
    public TransactionOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testCollectionAspect () {
        final PlatformTransactionManager    manager=new PlatformTransactionManagerImpl();
        final int                           timeout=(int) Thread.currentThread().getId();
        for (final TransactionOperationStatus opStatus : TransactionOperationStatus.values()) {
            for (final boolean readOnly : new boolean[] { true, false }) { 
                for (int propagationValue=0;
                        propagationValue < TransactionOperationFinalizer.propagationNames.size();
                        propagationValue++) {
                    final int propagationBehavior=propagationValue; // has to be final for use in the anonymous class
                    for (final int isolationLevel : new int[] { 
                                Connection.TRANSACTION_NONE,
                                Connection.TRANSACTION_READ_COMMITTED,
                                Connection.TRANSACTION_REPEATABLE_READ,
                                Connection.TRANSACTION_SERIALIZABLE
                            }) {
                        TransactionDefinition   txDefinition=new TransactionDefinition() {
                                public boolean isReadOnly() {
                                    return readOnly;
                                }
                                
                                public int getTimeout() {
                                    return timeout;
                                }
                                
                                public int getPropagationBehavior() {
                                    return propagationBehavior;
                                }
                                
                                public String getName() {
                                    return opStatus.name()
                                            + ";readOnly=" + readOnly
                                            + ";propagation=" + TransactionOperationFinalizer.propagationNames.get(propagationBehavior)
                                            + ";isolation=" + isolationLevel
                                            + ";timeout=" + timeout
                                              ;
                                }
                                
                                public int getIsolationLevel() {
                                    return isolationLevel;
                                }
                                
                                @Override
                                public String toString () {
                                    return getName();
                                }
                            };
                        TransactionStatus   txStatus=manager.getTransaction(txDefinition);
                       
                        switch(opStatus) {
                            case Committed :
                                manager.commit(txStatus);
                                break;
                            case RolledBack :
                                manager.rollback(txStatus);
                                break;
                            default :
                                fail("Unknown status action: " + opStatus);
                        }

                        assertTransactionOperation(txDefinition.getName(), opStatus, txDefinition);
                        Mockito.reset(spiedOperationCollector); // prepare for next iteration
                    }
                }
            }
        }
    }

    @Override
    public TransactionOperationCollectionAspect getAspect() {
        return TransactionOperationCollectionAspect.aspectOf();
    }

    protected Operation assertTransactionOperation (String  testName,
                                                    TransactionOperationStatus opStatus,
                                                    TransactionDefinition txDefinition) {
        Operation   op=getLastEntered();
        assertNotNull(testName + ": No operation extracted", op);
        assertEquals(testName + ": Mismatched operation type", TransactionOperationCollectionAspect.TYPE, op.getType());
        assertEquals(testName + ": Mismatched name", txDefinition.getName(), op.get("name", String.class));
        assertEquals(testName + ": Mismatched read only value",
                            Boolean.valueOf(txDefinition.isReadOnly()), op.get("readOnly", Boolean.class));
        assertEquals(testName + ": Mismatched timeout value",
                            Integer.valueOf(txDefinition.getTimeout()), op.get("timeout", Integer.class));

        Operation   dummyOp=new Operation()
                        .put("propagation", txDefinition.getPropagationBehavior())
                        .put("isolation", txDefinition.getIsolationLevel())
                        ;
        assertEquals(testName + ": Mismatched propagation value",
                            TransactionOperationFinalizer.normalizePropagation(dummyOp),
                            op.get("propagation", String.class));
        assertEquals(testName + ": Mismatched isolation value",
                            TransactionOperationFinalizer.normalizeIsolation(dummyOp),
                            op.get("isolation", String.class));
        return op;
    }
            
    static class PlatformTransactionManagerImpl implements PlatformTransactionManager {
        private final Logger    logger=Logger.getLogger(getClass().getName());
        PlatformTransactionManagerImpl () {
            super();
        }

        public TransactionStatus getTransaction(TransactionDefinition definition)
                throws TransactionException {
            logger.info("getTransaction(" + definition + ")");
            return new TransactionStatus() {
                public Object createSavepoint() throws TransactionException {
                    throw new TransactionSystemException("createSavepoint N/A");
                }

                public void rollbackToSavepoint(Object savepoint)
                        throws TransactionException {
                    throw new TransactionSystemException("rollbackToSavepoint N/A");
                }

                public void releaseSavepoint(Object savepoint)
                        throws TransactionException {
                    throw new TransactionSystemException("releaseSavepoint N/A");
                }

                public boolean isNewTransaction() {
                    return true;
                }

                public boolean hasSavepoint() {
                    return false;
                }

                public void setRollbackOnly() {
                    throw new TransactionSystemException("setRollbackOnly N/A");
                }

                public boolean isRollbackOnly() {
                    return false;
                }

                public void flush() {
                    throw new TransactionSystemException("flush N/A");
                }

                public boolean isCompleted() {
                    return true;
                }
            };
        }

        public void commit(TransactionStatus status)
                throws TransactionException {
            logger.info("commit()");            
        }
        public void rollback(TransactionStatus status)
                throws TransactionException {
            logger.info("rollback()");            
        }
    }
}
