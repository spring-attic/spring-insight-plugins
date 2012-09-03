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

package com.springsource.insight.plugin.jpa;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public class TransactionalJpaEntityManagerDomainAspectTest
        extends TransactionalJpaEntityManagerTestSupport {

    public TransactionalJpaEntityManagerDomainAspectTest() {
        super();
    }

    @Test
    public void testPersist () {
        entityManager.persist(new TestEntity());
        assertDomainOperation("testPersist", "persist");
    }

    @Test
    public void testRemove () {
        TestEntity  entity=createPersistedEntity();
        entityManager.remove(entity);
        assertDomainOperation("testRemove", "remove");
    }

    @Test
    public void testRefresh () {
        TestEntity  entity=createPersistedEntity();
        entity.setCreationDate(new Date(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(3600L)));
        entityManager.refresh(entity);
        assertDomainOperation("testRefresh", "refresh");
    }

    @Test
    public void testMerge() {
        TestEntity  entity=createPersistedEntity();
        entity.setCreationDate(new Date(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(3600L)));
        
        TestEntity  result=entityManager.merge(entity);
        Assert.assertNotNull("No merge result", result);
        assertDomainOperation("testMerge", "merge");
    }

    @Override
    public JpaEntityManagerDomainObjectAspect getAspect() {
        return JpaEntityManagerDomainObjectAspect.aspectOf();
    }

    protected Operation assertDomainOperation (String testName, String action) {
        Operation   op=assertManagerOperation(testName, action, JpaDefinitions.DOMAIN_GROUP);
        Assert.assertEquals(testName + ": Mismatched domain class",
                            TestEntity.class.getName(),
                            op.get(JpaDefinitions.DOMAIN_CLASS_ATTR, String.class));
        return op;
    }

    protected TestEntity createPersistedEntity () {
        entityManager.persist(new TestEntity());

        List<TestEntity>    el=findAllEntities();
        Assert.assertNotNull("No results list", el);
        Assert.assertTrue("No results data", el.size() > 0);
        return el.get(0);
    }

    protected List<TestEntity> findAllEntities () {
        return entityManager.createNamedQuery("TestEntity.findEntitiesInRange", TestEntity.class)
                            .setParameter("startTime", new Date(TimeUnit.SECONDS.toMillis(1L)))
                            .setParameter("endTime", new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3600L)))
                            .getResultList()
                            ;
    }
}
