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

package com.springsource.insight.plugin.springweb.controller;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.util.MapUtil;

/**
 *
 */
public abstract class AbstractControllerOperationCollectionAspectTestSupport
        extends OperationCollectionAspectTestSupport {
    private final Boolean legacyAPI;

    protected AbstractControllerOperationCollectionAspectTestSupport(boolean isLegacy) {
        legacyAPI = Boolean.valueOf(isLegacy);
    }

    protected Operation assertControllerOperation() {
        Operation op = getLastEntered();
        assertNotNull("No operation entered", op);
        assertEquals("Mismatched operation type", ControllerEndPointAnalyzer.CONTROLLER_METHOD_TYPE, op.getType());
        assertEquals("Mismatched legacy flag value", legacyAPI, op.get(ControllerEndPointAnalyzer.LEGACY_PROPNAME, Boolean.class));
        return op;
    }

    protected Operation assertEncodeReturnModelValues(TestSupportController controller) {
        return assertEncodeModelValues(ControllerOperationCollector.RETURN_VALUE_MODEL_MAP, controller.returnModel);
    }

    protected Operation assertControllerView(String expected) {
        Operation op = assertControllerOperation();
        assertControllerView(op, expected);
        return op;
    }

    protected static final String assertControllerView(Operation op, String expected) {
        String viewName = op.get(ControllerOperationCollector.RETURN_VALUE_VIEW_NAME, String.class);
        assertEquals("Mismatched view name", expected, viewName);
        return viewName;
    }

    protected Operation assertEncodeModelValues(String mapName, Map<String, ?> expected) {
        Operation op = assertControllerOperation();
        assertEncodeModelValues(op, mapName, expected);
        return op;
    }

    protected static final OperationMap assertEncodeModelValues(Operation op, String mapName, Map<String, ?> expected) {
        OperationMap map = op.get(mapName, OperationMap.class);
        assertNotNull(mapName + ": missing encoding", map);
        return assertEncodeModelValues(map, mapName, expected);
    }

    protected static final OperationMap assertEncodeModelValues(OperationMap map, String mapName, Map<String, ?> expected) {
        assertEquals(mapName + ": Mismatched size", MapUtil.size(expected), map.size());

        for (Map.Entry<String, ?> me : map.entrySet()) {
            String key = me.getKey();
            Object actualValue = me.getValue();
            Object expectedValue = ControllerOperationCollector.resolveCollectedValue(expected.get(key));
            assertEquals(mapName + ": Mismatched value for " + key, expectedValue, actualValue);
        }

        return map;
    }

    protected static final Map<String, Object> createTestModelMap(final String testName) {
        return new TreeMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("curDate", new Date());
                put("nanoTime", Long.valueOf(System.nanoTime()));
                put("testName", testName);
                put("boolValue", Boolean.valueOf((System.currentTimeMillis() & 0x01L) == 0L));
            }
        };
    }

    protected static class TestSupportController {
        protected final Map<String, ?> returnModel;
        protected final String returnView;

        TestSupportController(Map<String, ?> outgoingModel, String outgoingView) {
            returnModel = outgoingModel;
            returnView = outgoingView;
        }
    }
}
