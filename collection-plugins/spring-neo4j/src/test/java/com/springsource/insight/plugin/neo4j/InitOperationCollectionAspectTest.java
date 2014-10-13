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
package com.springsource.insight.plugin.neo4j;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.intercept.operation.Operation;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(OperationCollectionAspectTests.TEST_CONTEXT)
@Transactional
public class InitOperationCollectionAspectTest extends AbstractNeo4jCollectionAspectTestSupport {
    @Autowired
    OperationCollectionAspectTests tests;


    public InitOperationCollectionAspectTest() {
        super();
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return InitOperationCollectionAspect.aspectOf();
    }

    @Test
    public void testRun() throws Exception {
        // Step 1: Execute test
        tests.test_Init();

        // Step 2:  Get the Operation that was just created by our aspect
        Operation op = getLastEntered();
        assertNotNull("No Neo4J.Init operation data is intercepted", op);

        // Step 3:  Validate
        assertEquals("Invalid operation type", OperationCollectionTypes.INIT_TYPE.type, op.getType());

        assertEquals("Invalid Label", OperationCollectionTypes.INIT_TYPE.label, op.getLabel());

        assertNotNull("Parameter 'service' does not exists", op.get("service"));
    }
}