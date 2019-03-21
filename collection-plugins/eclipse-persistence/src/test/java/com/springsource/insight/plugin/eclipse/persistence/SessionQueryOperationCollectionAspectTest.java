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

package com.springsource.insight.plugin.eclipse.persistence;

import java.util.Collections;

import org.eclipse.persistence.internal.jpa.JPAQuery;
import org.eclipse.persistence.sessions.Session;
import org.junit.Test;
import org.mockito.Mockito;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;


/**
 * 
 */
public class SessionQueryOperationCollectionAspectTest
        extends EclipsePersistenceCollectionTestSupport {

    public SessionQueryOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testExecuteQuery () {
        for (QueryExecutor qe : QueryExecutor.values()) {
            qe.executeQuery(mockSession);
            
            Operation   op=assertQueryExecutionOperation(qe.name(), qe.name());
            qe.assertQueryArguments(op);
            Mockito.reset(spiedOperationCollector); // prepare for next iteration
        }
    }

    @Override
    public SessionQueryOperationCollectionAspect getAspect() {
        return SessionQueryOperationCollectionAspect.aspectOf();
    }

    protected Operation assertQueryExecutionOperation (String testName, String queryName) {
        return assertPersistenceOperation(testName, EclipsePersistenceDefinitions.QUERY, queryName);
    }

    static enum QueryExecutor {
        DIRECT(1) {
            @Override
            public Object executeQuery (Session session) {
                return session.executeQuery(name());
            }
        },
        DIRECT1ARG(2) {
            @Override
            public Object executeQuery (Session session) {
                return session.executeQuery(name(), this);
            }
        },
        DIRECT2ARGS(3) {
            @Override
            public Object executeQuery (Session session) {
                return session.executeQuery(name(), this, this);
            }
        },
        DIRECT3ARGS(4) {
            @Override
            public Object executeQuery (Session session) {
                return session.executeQuery(name(), this, this, this);
            }
        },
        DIRECTARGSLIST(2) {
            @Override
            public Object executeQuery (Session session) {
                return session.executeQuery(name(), Collections.singletonList(this));
            }
        },
        DOMAINONLY(2) {
            @Override
            public Object executeQuery (Session session) {
                return session.executeQuery(name(), getClass());
            }
        },
        DOMAIN1ARG(3) {
            @Override
            public Object executeQuery (Session session) {
                return session.executeQuery(name(), getClass(), this);
            }
        },
        DOMAIN2ARGS(4) {
            @Override
            public Object executeQuery (Session session) {
                return session.executeQuery(name(), getClass(), this, this);
            }
        },
        DOMAIN3ARGS(5) {
            @Override
            public Object executeQuery (Session session) {
                return session.executeQuery(name(), getClass(), this, this, this);
            }
        },
        DOMAINARGSLIST(3) {
            @Override
            public Object executeQuery (Session session) {
                return session.executeQuery(name(), getClass(), Collections.singletonList(this));
            }
        },
        DBQUERY(1) {
            @Override
            public Object executeQuery (Session session) {
                return session.executeQuery(new JPAQuery(name(), name(), null, Collections.<String,Object>emptyMap()));
            }
        },
        DBQUERYARGSLIST(2) {
            @Override
            public Object executeQuery (Session session) {
                return session.executeQuery(new JPAQuery(name(), name(), null, Collections.<String,Object>emptyMap()), Collections.singletonList(this));
            }
        };

        private final int   numArgs;
        QueryExecutor (@SuppressWarnings("hiding") int numArgs) {
            this.numArgs = numArgs;
        }

        public abstract Object executeQuery (Session session);

        // returns name of query in the arguments list
        public String assertQueryArguments (Operation op) {
            OperationList   argsList=op.get(OperationFields.ARGUMENTS, OperationList.class);
            assertNotNull(name() + ": No arguments extracted", argsList);
            assertEquals(name() + ": Mismatched number of arguments", numArgs, argsList.size());
            
            String  name=SessionQueryOperationJoinPointFinalizer.resolveQueryNameFromArgument(argsList.get(0));
            assertEquals(name() + ": Mismatched query name argument value", name(), name);
            return name;
        }
    }
}
