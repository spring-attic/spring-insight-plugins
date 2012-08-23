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

package com.springsource.insight.plugin.jpa;

import java.util.logging.Logger;

import javax.persistence.EntityTransaction;

import org.junit.Test;
import org.mockito.Mockito;

import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;

/**
 * 
 */
public class EntityTransactionCollectionAspectTest
        extends OperationCollectionAspectTestSupport {
    public EntityTransactionCollectionAspectTest() {
        super();
    }

    @Test
    public void testTransactionAction () {
        for (TransactionAction action : TransactionAction.values()) {
            action.execute(mockTransaction);

            String      actionName=action.name().toLowerCase();
            Operation   op=getLastEntered();
            assertNotNull(actionName + ": No operation extracted", op);
            assertEquals(actionName + ": Mismatched operation type", JpaDefinitions.TX_ENTITY, op.getType());
            assertEquals(actionName + ": Mismatched action name", actionName, op.get(OperationFields.METHOD_NAME,String.class));
            Mockito.reset(spiedOperationCollector); // prepare for next iteration
        }
    }

    @Override
    public EntityTransactionCollectionAspect getAspect() {
        return EntityTransactionCollectionAspect.aspectOf();
    }

    private static enum TransactionAction {
        BEGIN {
            @Override
            public void execute (EntityTransaction transaction) {
                transaction.begin();
            }
        },
        COMMIT {
            @Override
            public void execute (EntityTransaction transaction) {
                transaction.commit();
            }
        },
        ROLLBACK {
            @Override
            public void execute (EntityTransaction transaction) {
                transaction.rollback();
            }
        };
        
        public abstract void execute (EntityTransaction transaction);
    }

    private static final EntityTransaction mockTransaction=new EntityTransaction() {
        private final Logger    logger=Logger.getLogger(EntityTransaction.class.getName());
        public void begin() {
            logger.info("BEGIN");
        }

        public void commit() {
            logger.info("COMMIT");
        }

        public void rollback() {
            logger.info("ROLLBACK");
        }

        private boolean rollbackOnly;
        public void setRollbackOnly() {
            rollbackOnly = true;
        }

        public boolean getRollbackOnly() {
            return rollbackOnly;
        }

        public boolean isActive() {
            return true;
        }
    };
}
