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

package com.springsource.insight.plugin.jpa;

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
import com.springsource.insight.collection.OperationListCollector;
import com.springsource.insight.intercept.operation.Operation;

@TransactionConfiguration
@ContextConfiguration(locations = {"classpath:META-INF/jpaTestContext.xml"})
public abstract class TransactionalJpaEntityManagerTestSupport
        extends AbstractTransactionalJUnit4SpringContextTests {
    @PersistenceContext
    protected EntityManager entityManager;
    private OperationCollector originalCollector;
    private final OperationListCollector spiedCollector = new OperationListCollector();

    protected TransactionalJpaEntityManagerTestSupport() {
        super();
    }

    @Before
    public void setUp() {
        OperationCollectionAspectSupport aspectInstance = getAspect();
        originalCollector = aspectInstance.getCollector();
        spiedCollector.clearCollectedOperations();
        aspectInstance.setCollector(spiedCollector);
    }

    @After
    public void tearDown() {
        OperationCollectionAspectSupport aspectInstance = getAspect();
        aspectInstance.setCollector(originalCollector);
    }

    protected abstract OperationCollectionAspectSupport getAspect();

    protected Operation assertManagerOperation(String testName, String action, String opGroup) {
        return JpaEntityManagerCollectionTestSupport.assertManagerOperation(getLastEntered(), testName, action, opGroup);
    }

    protected Operation getLastEntered() {
        List<? extends Operation> opsList = spiedCollector.getCollectedOperations();
        int numOps = opsList.size();
        if (numOps <= 0) {
            return null;
        }

        return opsList.get(numOps - 1);
    }
}
