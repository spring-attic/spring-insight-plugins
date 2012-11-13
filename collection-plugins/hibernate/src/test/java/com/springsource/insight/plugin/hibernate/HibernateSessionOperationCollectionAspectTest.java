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
package com.springsource.insight.plugin.hibernate;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

public class HibernateSessionOperationCollectionAspectTest 
   		extends OperationCollectionAspectTestSupport {
	public HibernateSessionOperationCollectionAspectTest () {
		super();
	}

    @Test
    public void flushAndCollect() {
        new DummySessionImpl().flush();
        standardAsserts("flush");
    }
    
    @Test
    public void saveAndCollect() {
        new DummySessionImpl().save(null);
        standardAsserts("save");
    }
    
    @Test
    public void loadAndCollect() {
        new DummySessionImpl().load("dummy", null);
        standardAsserts("load");
    }
    
    @Test
    public void deleteAndCollect() {
        new DummySessionImpl().delete(null);
        standardAsserts("delete");
    }

    @Test
    public void updateAndCollect() {
        new DummySessionImpl().update(null);
        standardAsserts("update");
    }
    
    @Test
    public void getAndCollect() {
        new DummySessionImpl().get("", null);
        standardAsserts("get");
    }
    
    @Test
    public void isDirtyAndCollect() {
        new DummySessionImpl().isDirty();
        standardAsserts("isDirty");
    }
    
    @Override
    public HibernateSessionOperationCollectionAspect getAspect() {
        return HibernateSessionOperationCollectionAspect.aspectOf();
    }

	private void standardAsserts(String method) {
        Operation op = getLastEntered();
        assertNotNull("No operation collected", op);
        assertEquals("Mismatched operation type", HibernateSessionOperationCollectionAspect.TYPE, op.getType());
        assertEquals("Mismatched label", "Hibernate Session." + method, op.getLabel());
        assertEquals("Mismatched collection count", DummySessionImpl.DUMMY_COLLECTION_COUNT, op.getInt("collectionCount", (-1)));
        assertEquals("Mismatched entity count", DummySessionImpl.DUMMY_ENTITY_COUNT, op.getInt("entityCount", (-1)));
        assertEquals("Mismatched flush mode", DummySessionImpl.DUMMY_FLUSH_MODE.toString(), op.get("flushMode", String.class));
	}
}
