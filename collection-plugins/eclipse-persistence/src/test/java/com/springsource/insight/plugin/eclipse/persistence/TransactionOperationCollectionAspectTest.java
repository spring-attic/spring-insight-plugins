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

package com.springsource.insight.plugin.eclipse.persistence;

import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.junit.Test;
import org.mockito.Mockito;

import com.springsource.insight.intercept.operation.Operation;


/**
 * 
 */
public class TransactionOperationCollectionAspectTest
        extends EclipsePersistenceCollectionTestSupport {

    public TransactionOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testTransactionOperation() {
        for (TransactionOperationAction action : TransactionOperationAction.values()) {
            action.executeAction(mockSession);
            assertTransactionOperation(action.name(), action.name().toLowerCase());
            Mockito.reset(spiedOperationCollector); // prepare for next action
        }
    }

    @Override
    public TransactionOperationCollectionAspect getAspect() {
        return TransactionOperationCollectionAspect.aspectOf();
    }

    protected Operation assertTransactionOperation (String testName, String action) {
        return assertPersistenceOperation(testName, EclipsePersistenceDefinitions.TX, action);
    }

    static enum TransactionOperationAction {
        BEGIN {
            @Override
            public void executeAction (DatabaseSession session) throws DatabaseException {
                session.beginTransaction();
            }
        },
        COMMIT {
            @Override
            public void executeAction (DatabaseSession session) throws DatabaseException {
                session.commitTransaction();
            }
        },
        ROLLBACK {
            @Override
            public void executeAction (DatabaseSession session) throws DatabaseException {
                session.rollbackTransaction();
            }
        };
        
        public abstract void executeAction (DatabaseSession session) throws DatabaseException;
    }
}
