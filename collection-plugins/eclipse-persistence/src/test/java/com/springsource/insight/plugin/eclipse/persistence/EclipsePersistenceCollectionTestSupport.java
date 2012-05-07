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

import org.eclipse.persistence.sessions.DatabaseSession;
import org.junit.Assert;

import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

/**
 * 
 */
public abstract class EclipsePersistenceCollectionTestSupport
            extends OperationCollectionAspectTestSupport {
    protected final DatabaseSession mockSession=new MockDatabaseSession();
    protected EclipsePersistenceCollectionTestSupport() {
        super();
    }

    protected Operation assertPersistenceOperation (String testName, OperationType opType, String action) {
        Operation   op=getLastEntered();
        Assert.assertNotNull(testName + ": No operation extracted", op);
        Assert.assertEquals(testName + ": Mismatched operation type", opType, op.getType());
        Assert.assertEquals(testName + ": Mismatched action value", action, op.get(EclipsePersistenceDefinitions.ACTION_ATTR,String.class));
        return op;
    }
}
