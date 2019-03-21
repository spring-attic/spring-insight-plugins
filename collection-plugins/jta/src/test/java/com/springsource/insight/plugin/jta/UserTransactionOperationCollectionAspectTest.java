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
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.junit.Test;


/**
 *
 */
public class UserTransactionOperationCollectionAspectTest
        extends JtaOperationCollectionAspectTestSupport {
    static final UserTransaction transaction = new UserTransactionImpl();

    public UserTransactionOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testUserTransactionOperations() {
        runAspectOperations(UserTransactionOperation.class);
    }

    @Override
    public UserTransactionOperationCollectionAspect getAspect() {
        return UserTransactionOperationCollectionAspect.aspectOf();
    }

    static enum UserTransactionOperation implements Runnable, ParameterTypeDescriptor {
        BEGIN {
            @Override
            protected void doOperation(UserTransaction tx) throws Exception {
                tx.begin();
            }
        },
        COMMIT {
            @Override
            protected void doOperation(UserTransaction tx) throws Exception {
                tx.commit();
            }
        },
        ROLLBACK {
            @Override
            protected void doOperation(UserTransaction tx) throws Exception {
                tx.rollback();
            }
        };

        protected abstract void doOperation(UserTransaction tx) throws Exception;

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

    static class UserTransactionImpl implements UserTransaction {
        private final Logger logger = Logger.getLogger(getClass().getName());

        UserTransactionImpl() {
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

        public void rollback() throws IllegalStateException, SecurityException, SystemException {
            logger.info("rollback()");
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
            throw new SystemException("setRollbackOnly N/A");
        }

        public int getStatus() throws SystemException {
            throw new SystemException("getStatus N/A");
        }

        public void setTransactionTimeout(int seconds) throws SystemException {
            throw new SystemException("setTransactionTimeout N/A");
        }
    }
}
