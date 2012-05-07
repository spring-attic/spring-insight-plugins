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

package com.springsource.insight.plugin.jta;

import java.util.logging.Logger;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.junit.Test;
import org.mockito.Mockito;


/**
 * 
 */
public class TransactionManagerOperationCollectionAspectTest
        extends JtaOperationCollectionAspectTestSupport {
    static final TransactionManager manager=new TransactionManagerImpl();

    public TransactionManagerOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testTransactionManagerOperations () {
        runAspectOperations(TransactionManagerOperation.class);
    }

    @Override
    public TransactionManagerOperationCollectionAspect getAspect() {
        return TransactionManagerOperationCollectionAspect.aspectOf();
    }

    static enum TransactionManagerOperation implements Runnable, ParameterTypeDescriptor {
        BEGIN {
            @Override
            protected void doOperation (TransactionManager mgr) throws Exception {
                mgr.begin();
            }
        },
        COMMIT {
            @Override
            protected void doOperation (TransactionManager mgr) throws Exception {
                mgr.commit();
            }
        },
        ROLLBACK {
            @Override
            protected void doOperation (TransactionManager mgr) throws Exception {
                mgr.rollback();
            }
        },
        SUSPEND {
            @Override
            protected void doOperation (TransactionManager mgr) throws Exception {
                mgr.suspend();
            }

        },
        RESUME {
            @Override
            protected void doOperation (TransactionManager mgr) throws Exception {
                mgr.resume(Mockito.mock(Transaction.class));
            }

            @Override
            public Class<?>[] getArgTypes() {
                return new Class<?>[] { Transaction.class };
            }
        };

        protected abstract void doOperation (TransactionManager mgr) throws Exception;

        public void run () {
            try {
                doOperation(manager);
            } catch(Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }

        public Class<?>[] getArgTypes() {
            return EMPTY_CLASSES;
        }
    }

    static class TransactionManagerImpl implements TransactionManager {
        private final Logger    logger=Logger.getLogger(getClass().getName());
        TransactionManagerImpl () {
            super();
        }

        public void begin() throws NotSupportedException, SystemException {
            logger.info("begin()");
        }

        public void commit() throws RollbackException, HeuristicMixedException,
                HeuristicRollbackException, SecurityException,
                IllegalStateException, SystemException {
            logger.info("commit()");
        }

        public int getStatus() throws SystemException {
            throw new SystemException("getStatus N/A");
        }

        public Transaction getTransaction() throws SystemException {
            throw new SystemException("getTransaction N/A");
        }

        public void resume(Transaction tobj)
                throws InvalidTransactionException, IllegalStateException, SystemException {
            logger.info("resume()");
        }

        public void rollback() throws IllegalStateException, SecurityException, SystemException {
            logger.info("rollback()");
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
            throw new SystemException("setRollbackOnly N/A");
        }

        public void setTransactionTimeout(int seconds) throws SystemException {
            throw new SystemException("setTransactionTimeout N/A");
        }

        public Transaction suspend() throws SystemException {
            logger.info("suspend()");
            return Mockito.mock(Transaction.class);
        }
    }
}
