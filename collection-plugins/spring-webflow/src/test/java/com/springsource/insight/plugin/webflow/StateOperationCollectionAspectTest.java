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

package com.springsource.insight.plugin.webflow;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;


public class StateOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
    public StateOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testState() {
        // Step 1: Execute test
        WebFlowExecutionTest webFlow = new WebFlowExecutionTest();
        webFlow.testState();

        // Step 2:  Get the Operation that was just created by our aspect
        Operation op = getLastEntered();

        // Step 3:  Validate
        assertNotNull(op);
        assert op.getType().getName().equals("wf-state");

        assert "ViewState".equals(op.get("stateType"));
        assert "dummy2".equals(op.get("stateId"));
        //assert "/dummyView".equals(op.get("view"));
        OperationMap map = (OperationMap) op.get("attribs");
        assertNotNull(map.get("model"));

        OperationList entryActions = (OperationList) op.get("entryActions");
        assert "personDao.save(person)".equals(entryActions.get(0));
        assert "flowScope.temp=1".equals(entryActions.get(1));
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return StateOperationCollectionAspect.aspectOf();
    }
}