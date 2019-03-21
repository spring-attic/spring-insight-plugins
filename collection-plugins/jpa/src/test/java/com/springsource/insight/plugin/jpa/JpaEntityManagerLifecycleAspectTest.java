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

import javax.persistence.EntityManager;

import org.junit.Test;
import org.mockito.Mockito;

import com.springsource.insight.intercept.operation.Operation;


/**
 *
 */
public class JpaEntityManagerLifecycleAspectTest
        extends JpaEntityManagerCollectionTestSupport {

    public JpaEntityManagerLifecycleAspectTest() {
        super();
    }

    @Test
    public void testLifecyleActions() {
        for (LifecycleAction action : LifecycleAction.values()) {
            String testName = "testLifecyleActions(" + action + ")";
            action.executeAction(entityManager);
            assertManagerOperation(testName, action.getActionName());
            Mockito.reset(spiedOperationCollector); // prepare for next iteration
        }
    }

    @Override
    public JpaEntityManagerLifecycleAspect getAspect() {
        return JpaEntityManagerLifecycleAspect.aspectOf();
    }

    protected Operation assertManagerOperation(String testName, String action) {
        return assertManagerOperation(testName, action, JpaDefinitions.LIFECYCLE_GROUP);
    }

    static enum LifecycleAction {
        FLUSH {
            @Override
            public void executeAction(EntityManager em) {
                em.flush();
            }
        },
        CLEAR {
            @Override
            public void executeAction(EntityManager em) {
                em.clear();
            }
        },
        JOIN {
            @Override
            public void executeAction(EntityManager em) {
                em.joinTransaction();
            }

            @Override
            public String getActionName() {
                return "joinTransaction";
            }
        },
        CLOSE {
            @Override
            public void executeAction(EntityManager em) {
                em.close();
            }
        };

        public abstract void executeAction(EntityManager em);

        public String getActionName() {
            return name().toLowerCase();
        }
    }

}
