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

package com.springsource.insight.plugin.jta;

import java.util.logging.Logger;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.junit.Test;


/**
 *
 */
public class TransactionOperationCollectionAspectTest
        extends JtaOperationCollectionAspectTestSupport {
    static final Transaction transaction = new TransactionImpl();

    public TransactionOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testTransactionOperations() {
        runAspectOperations(TransactionOperation.class);
    }

    @Override
    public TransactionOperationCollectionAspect getAspect() {
        return TransactionOperationCollectionAspect.aspectOf();
    }

    static enum TransactionOperation implements Runnable, ParameterTypeDescriptor {
        COMMIT {
            @Override
            protected void doOperation(Transaction tx) throws Exception {
                tx.commit();
            }
        },
        ROLLBACK {
            @Override
            protected void doOperation(Transaction tx) throws Exception {
                tx.rollback();
            }
        };

        protected abstract void doOperation(Transaction tx) throws Exception;

        public void run() {
            try {
                doOperation(transaction);
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }

        public final Class<?>[] getArgTypes() {
            return EMPTY_CLASSES;
        }
    }

    static class TransactionImpl implements Transaction {
        private final Logger logger = Logger.getLogger(getClass().getName());

        TransactionImpl() {
            super();
        }

        public void commit() throws RollbackException, HeuristicMixedException,
                HeuristicRollbackException, SecurityException,
                IllegalStateException, SystemException {
            logger.info("commit()");
        }

        public boolean delistResource(XAResource xaRes, int flag)
                throws IllegalStateException, SystemException {
            throw new SystemException("delistResource - N/A");
        }

        public boolean enlistResource(XAResource xaRes)
                throws RollbackException, IllegalStateException, SystemException {
            throw new SystemException("enlistResource - N/A");
        }

        public int getStatus() throws SystemException {
            throw new SystemException("getStatus - N/A");
        }

        public void registerSynchronization(Synchronization sync)
                throws RollbackException, IllegalStateException, SystemException {
            throw new SystemException("registerSynchronization - N/A");
        }

        public void rollback() throws IllegalStateException, SystemException {
            logger.info("rollback()");
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
            throw new SystemException("setRollbackOnly - N/A");
        }
    }
}
