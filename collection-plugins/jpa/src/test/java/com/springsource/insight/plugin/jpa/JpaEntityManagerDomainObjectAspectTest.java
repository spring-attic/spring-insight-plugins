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

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public class JpaEntityManagerDomainObjectAspectTest
        extends JpaEntityManagerCollectionTestSupport {

    public JpaEntityManagerDomainObjectAspectTest() {
        super();
    }

    @Test
    public void testResolveDomainClassOnNoArgs () {
        Assert.assertEquals("Mismatched null result",
                            void.class,
                            JpaEntityManagerDomainObjectAspect.resolveDomainClass((Object[]) null));
        Assert.assertEquals("Mismatched empty result",
                            void.class,
                            JpaEntityManagerDomainObjectAspect.resolveDomainClass(new Object[] { }));
    }

    @Test
    public void testResolveDomainClassOnArguments () {
        Assert.assertSame("Mismatched result on one argument",
                          String.class,
                          JpaEntityManagerDomainObjectAspect.resolveDomainClass("123456"));
        Assert.assertSame("Mismatched result on multiple arguments",
                          Long.class,
                          JpaEntityManagerDomainObjectAspect.resolveDomainClass(
                                  Long.valueOf(System.currentTimeMillis()), "123456", Runtime.getRuntime()));
    }

    @Test
    public void testDomainObjectActions () {
        for (DomainObjectAction action : DomainObjectAction.values()) {
            String  testName="testDomainObjectActions(" + action + ")";
            Object  entity=new TestEntity();
            action.executeAction(entityManager, entity);

            Operation   op=assertManagerOperation(testName, action.getActionName());
            Assert.assertEquals(testName + ": Mismatched domain class",
                                entity.getClass().getName(),
                                op.get(JpaDefinitions.DOMAIN_CLASS_ATTR, String.class));
            Mockito.reset(spiedOperationCollector); // prepare for next iteration
        }
    }

    @Override
    public JpaEntityManagerDomainObjectAspect getAspect() {
        return JpaEntityManagerDomainObjectAspect.aspectOf();
    }

    protected Operation assertManagerOperation (String testName, String action) {
        return assertManagerOperation(testName, action, JpaDefinitions.DOMAIN_GROUP);
    }

    static enum DomainObjectAction {
        PERSIST {
            @Override
            public void executeAction (EntityManager em, Object entity) {
                em.persist(entity);
            }
        },
        MERGE {
            @Override
            public void executeAction (EntityManager em, Object entity) {
                em.merge(entity);
            }
        },
        REMOVE {
            @Override
            public void executeAction (EntityManager em, Object entity) {
                em.remove(entity);
            }
        },
        LOCK {
            @Override
            public void executeAction (EntityManager em, Object entity) {
                em.lock(entity, LockModeType.READ);
            }
        },
        REFRESH {
            @Override
            public void executeAction (EntityManager em, Object entity) {
                em.refresh(entity);
            }
        };
        
        public abstract void executeAction (EntityManager em, Object entity);

        public String getActionName () {
            return name().toLowerCase();
        }
    }
}
