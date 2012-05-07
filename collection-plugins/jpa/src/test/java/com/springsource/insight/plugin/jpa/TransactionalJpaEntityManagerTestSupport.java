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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.After;
import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.intercept.operation.Operation;

@TransactionConfiguration
@ContextConfiguration(locations={ "classpath:META-INF/jpaTestContext.xml" })
public abstract class TransactionalJpaEntityManagerTestSupport 
        extends AbstractTransactionalJUnit4SpringContextTests {
    @PersistenceContext
    protected EntityManager   entityManager;
    private OperationCollector  originalCollector;
    private final OperationListCollector    spiedCollector=new OperationListCollector();

    protected TransactionalJpaEntityManagerTestSupport() {
        super();
    }

    @Before
    public void setUp () {
        OperationCollectionAspectSupport    aspectInstance=getAspect();
        originalCollector = aspectInstance.getCollector();
        spiedCollector.clearCollectedOperations();
        aspectInstance.setCollector(spiedCollector);
    }

    @After
    public void tearDown () {
        OperationCollectionAspectSupport    aspectInstance=getAspect();
        aspectInstance.setCollector(originalCollector);
    }

    protected abstract OperationCollectionAspectSupport getAspect ();

    protected Operation assertManagerOperation (String testName, String action, String opGroup) {
        return JpaEntityManagerCollectionTestSupport.assertManagerOperation(getLastEntered(), testName, action, opGroup);
    }

    protected Operation getLastEntered () {
        List<? extends Operation>   opsList=spiedCollector.getCollectedOperations();
        int                         numOps=opsList.size();
        if (numOps <= 0) {
            return null;
        }
        
        return opsList.get(numOps - 1);
    }

    static class OperationListCollector implements OperationCollector {
        private final List<Operation>   _ops=Collections.synchronizedList(new ArrayList<Operation>());
        public OperationListCollector() {
            super();
        }

        public List<Operation> getCollectedOperations () {
            return _ops;
        }

        public void clearCollectedOperations () {
            _ops.clear();
        }

        public void enter(Operation operation) {
            _ops.add(operation);
        }

        public void exitNormal() {
            exitNormal(Void.class);
        }

        public void exitNormal(Object returnValue) {
            exitAbnormal(null);
        }

        public void exitAbnormal(Throwable throwable) {
            if (_ops.isEmpty()) {
                throw new IllegalStateException("Imbalanced stack frame");
            }
        }

        public void exitAndDiscard() {
            exitAndDiscard(Void.class);
        }

        public void exitAndDiscard(Object returnValue) {
            exitNormal(returnValue);

            if (_ops.isEmpty()) {
                throw new IllegalStateException("Imbalanced stack frame");
            }

            _ops.remove(_ops.size() - 1);
        }
    }
}
