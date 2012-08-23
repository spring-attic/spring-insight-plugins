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

package com.springsource.insight.plugin.eclipse.persistence;

import static java.util.Collections.unmodifiableSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.persistence.exceptions.DatabaseException;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Login;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.trace.ObscuredValueMarker;


/**
 * 
 */
public class DatabaseSessionOperationCollectionAspectTest
        extends EclipsePersistenceCollectionTestSupport {
    private ObscuredValueMarker originalMarker;
    public DatabaseSessionOperationCollectionAspectTest() {
        super();
    }

    @Before
    @Override
    public void setUp () {
        super.setUp();
        
        DatabaseSessionOperationCollectionAspect    aspectInstance=getAspect();
        originalMarker = aspectInstance.getSensitiveValueMarker();
        aspectInstance.setSensitiveValueMarker(new DummyObscuredValueMarker());
    }

    @After
    @Override
    public void restore() {
        DatabaseSessionOperationCollectionAspect    aspectInstance=getAspect();
        aspectInstance.setSensitiveValueMarker(originalMarker);
        super.restore();
    }

    @Test
    public void testLogin () {
        DatabaseSessionOperationCollectionAspect    aspectInstance=getAspect();
        DummyObscuredValueMarker                    marker=(DummyObscuredValueMarker) aspectInstance.getSensitiveValueMarker();
        for (LoginAction action : LoginAction.values()) {
            Login   mockLogin=Mockito.mock(Login.class);
            Mockito.when(mockLogin.getUserName()).thenReturn("username:" + action.name());
            Mockito.when(mockLogin.getPassword()).thenReturn("password:" + action.name());
            mockSession.setLogin(mockLogin);
            action.executeAction(mockSession, mockLogin);

            Operation   op=assertDatabaseSessionOperation(action.name(), "login");
            action.assertExecutionResult(op, mockLogin, marker.getValues());

            // prepare for next iteration
            Mockito.reset(spiedOperationCollector);
            marker.reset();
        }
    }

    @Test
    public void testLogout () {
        mockSession.logout();
        assertDatabaseSessionOperation("testLogout", "logout");
    }

    @Override
    public DatabaseSessionOperationCollectionAspect getAspect() {
        return DatabaseSessionOperationCollectionAspect.aspectOf();
    }

    protected Operation assertDatabaseSessionOperation (String testName, String action) {
        return assertPersistenceOperation(testName, EclipsePersistenceDefinitions.DB, action);
    }

    static enum LoginAction {
        LOGINEMPTY(0,false) {
            @Override
            public void executeAction (DatabaseSession session, Login login) throws DatabaseException {
                session.login();
            }
        },
        LOGINLOGIN(1,false) {
            @Override
            public void executeAction (DatabaseSession session, Login login) throws DatabaseException {
                session.login(login);
            }
        },
        LOGINUSERPASS(2,true) {
            @Override
            public void executeAction (DatabaseSession session, Login login) throws DatabaseException {
                session.login(login.getUserName(), login.getPassword());
            }
        };

        private final int   numArgs;
        private final boolean   obscured;

        LoginAction(@SuppressWarnings("hiding") int numArgs, @SuppressWarnings("hiding") boolean obscured) {
            this.numArgs = numArgs;
            this.obscured = obscured;
        }

        public abstract void executeAction (DatabaseSession session, Login login) throws DatabaseException;

        public OperationList assertExecutionResult (Operation op, Login login, Collection<?> markedValues) {
            OperationList   argsList=op.get(OperationFields.ARGUMENTS, OperationList.class);
            assertNotNull(name() + ": No arguments extracted", argsList);
            assertEquals(name() + ": Mismatched number of arguments", numArgs, argsList.size());
            if (obscured) {
                assertTrue(name() + ": username not obscured", markedValues.contains(login.getUserName()));
                assertTrue(name() + ": password not obscured", markedValues.contains(login.getUserName()));
            }

            return argsList;
        }
    }

    static class DummyObscuredValueMarker implements ObscuredValueMarker {
        private final Set<Object> objects=new HashSet<Object>();
        public Set<Object> getValues() {
            return unmodifiableSet(objects);
        }

        public void markObscured(Object o) {
            objects.add(o);
        }
        
        public void reset () {
            if (!objects.isEmpty()) {
                objects.clear();
            }
        }
    }

}
