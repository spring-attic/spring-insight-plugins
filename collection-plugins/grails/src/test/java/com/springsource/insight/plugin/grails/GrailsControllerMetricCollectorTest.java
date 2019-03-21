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

package com.springsource.insight.plugin.grails;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.SourceCodeLocation;
import com.springsource.insight.util.KeyValPair;

/**
 * 
 */
public class GrailsControllerMetricCollectorTest extends Assert {
    private final GrailsControllerMetricCollector   collector=new GrailsControllerMetricCollector();

    public GrailsControllerMetricCollectorTest() {
        super();
    }

    @Before
    public void setUp () {
        // make sure starting with no state
        GrailsControllerStateKeeper.getAndDestroyThreadLocalState();
    }

    @Test
    public void testProcessAbnormalExitWithoutState () {
        Operation   op=new Operation();
        collector.processAbnormalExit(op, new Throwable());
        assertUnknownOperationContents(op);
    }

    @Test
    public void testProcessAbnormalExitWithoutRequest () {
        GrailsControllerStateKeeper.setThreadLocalController(getClass().getSimpleName(), getClass().getName());
        Operation   op=new Operation();
        collector.processAbnormalExit(op, new Throwable());
        assertOperationContents(op,
                getClass().getSimpleName(),
                getClass().getName(),
                GrailsControllerMetricCollector.UNKNOWN_ACTION);
    }

    @Test
    public void testProcessNormalExitWithoutSetThreadLocalController() {
        KeyValPair<String, String>  kvp=new KeyValPair<String, String>(getClass().getSimpleName(), "testProcessNormalExitWithoutSetThreadLocalController");
        GrailsControllerStateKeeper.setThreadLocalActionParams(Collections.singletonList(kvp));
        Operation   op=new Operation();
        collector.processNormalExit(op, Object.class);

        assertUnknownOperationContents(op);
        OperationList actionParams=op.get("actionParams", OperationList.class);
        assertNotNull("No action params", actionParams);
        assertEquals("No params values", 1, actionParams.size());
        
        OperationMap    map=actionParams.get(0, OperationMap.class);
        assertNotNull("No values map", map);
        assertEquals("Mismatched params values entries", 2, map.size());
        assertEquals("Mismatched param key", kvp.getKey(), map.get("key", String.class));
        assertEquals("Mismatched param value", kvp.getValue(), map.get("value", String.class));
    }

    @Test
    public void testProcessNormalExitWithoutSetThreadLocalThreadLocalActionParams() {
        GrailsControllerStateKeeper.setThreadLocalController(getClass().getSimpleName(), getClass().getName());
        Operation   op=new Operation();
        collector.processNormalExit(op, Object.class);
        
        assertOperationContents(op,
                getClass().getSimpleName(),
                getClass().getName(),
                GrailsControllerMetricCollector.UNKNOWN_ACTION);

        OperationList actionParams=op.get("actionParams", OperationList.class);
        assertNotNull("No action params", actionParams);
        assertEquals("Non-empty action params", 0, actionParams.size());
    }

    @Test
    public void testProcessNormalExitWithoutState() {
        Operation   op=new Operation();
        collector.processNormalExit(op, Object.class);
        assertUnknownOperationContents(op);
    }

    private static Operation assertUnknownOperationContents (Operation op) {
        return assertOperationContents(op,
                GrailsControllerMetricCollector.UNKNOWN_CONTROLLER,
                GrailsControllerMetricCollector.UNKNOWN_CONTROLLER,
                GrailsControllerMetricCollector.UNKNOWN_ACTION);
    }

    private static Operation assertOperationContents (Operation op,
                                                      String    shortName,
                                                      String    fullName,
                                                      String    actionName) {
        assertEquals("Mismatched label", shortName + "#" + actionName, op.getLabel());
   
        SourceCodeLocation  scl=op.getSourceCodeLocation();
        assertEquals("Mismatched class name", fullName, scl.getClassName());
        assertEquals("Mismatched method name", actionName, scl.getMethodName());
        return op;
    }
}
