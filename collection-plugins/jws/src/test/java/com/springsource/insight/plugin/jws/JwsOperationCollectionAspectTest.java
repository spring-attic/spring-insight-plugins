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
package com.springsource.insight.plugin.jws;

import java.util.Date;

import javax.jws.WebParam;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;

/**
 * 
 */
public class JwsOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
    private final JwsServiceInstance    _testService;
    public JwsOperationCollectionAspectTest() {
        _testService = new JwsServiceInstance();
    }
    /*
     * @see com.springsource.insight.collection.OperationCollectionAspectTestSupport#getAspect()
     */
    @Override   // co-variant return
    public JwsOperationCollectionAspect getAspect() {
        return JwsOperationCollectionAspect.aspectOf();
    }

    @Test
    public void testRootPath () {
        runTestJwsService(_testService, JwsServiceDefinitions.NOW_CALL);
    }

    @Test
    public void testYesterdayPath () {
        runTestJwsService(_testService, JwsServiceDefinitions.YESTERDAY_CALL);
    }

    @Test
    public void testTomorrowPath () {
        runTestJwsService(_testService, JwsServiceDefinitions.TOMORROW_CALL);
    }

    private void runTestJwsService (final JwsServiceInstance testService, final String callType) {
        final Date  expDate, actDate;
        final long  now=System.currentTimeMillis();
        if (JwsServiceDefinitions.NOW_CALL.equals(callType)) {
            expDate = testService.getCurrentDate();
            actDate = new Date(now);
        } else if (JwsServiceDefinitions.YESTERDAY_CALL.equals(callType)) {
            expDate = testService.getYesterdayDate(now, true);
            actDate = new Date(now - 86400000L);
        } else if (JwsServiceDefinitions.TOMORROW_CALL.equals(callType)) {
            expDate = testService.getTomorrowDate(now, false);
            actDate = new Date(now + 86400000L);
        } else {
            fail("Unknown call type: " + callType);
            return;
        }

        final ArgumentCaptor<Operation> operationCaptor=ArgumentCaptor.forClass(Operation.class);
        Mockito.verify(spiedOperationCollector).enter(operationCaptor.capture());

        final Operation op=operationCaptor.getValue();
        assertNotNull("No operation extracted", op);
        assertEquals("Mismatched operation type(s)", JwsDefinitions.TYPE, op.getType());

        if (op.isFinalizable()) {
            op.finalizeConstruction();
        }

        final long  valsDiff=Math.abs(expDate.getTime() - actDate.getTime());
        assertTrue("Mismatched call return values", valsDiff < 5000L);

        assertServiceInformation(callType, op);

        if (JwsServiceDefinitions.NOW_CALL.equals(callType)) {
            return; // no parameters or annotations
        }

        assertWebMethodInformation(callType, op);
    }

    private void assertServiceInformation (final String callType, final Operation op) {
        assertOperationValue(callType, op, "name", "name", JwsServiceDefinitions.SERVICE_NAME);
        assertOperationValue(callType, op, "target namespace", "targetNamespace", JwsServiceDefinitions.TARGET_NAMESPACE);
        assertOperationValue(callType, op, "service name", "serviceName", JwsServiceDefinitions.SERVICE_NAME);
        assertOperationValue(callType, op, "port name", "portName", JwsServiceDefinitions.PORT_NAME);
        assertOperationValue(callType, op, "WSDL location", "wsdlLocation", JwsServiceDefinitions.WSDL_LOCATION);
        assertOperationValue(callType, op, "endpoint", "endpointInterface", JwsServiceDefinitions.ENDPOINT);
    }

    private void assertWebMethodInformation (final String callType, final Operation op) {
        assertOperationValue(callType, op, "op. name", "operationName", callType + JwsServiceDefinitions.OPERATION_SUFFIX);
        assertOperationValue(callType, op, "op. action", "action", callType + JwsServiceDefinitions.ACTION_SUFFIX);
        assertOperationValue(callType, op, "op. exclude", "exclude", Boolean.valueOf(JwsServiceDefinitions.EXCLUDE_METHOD), Boolean.class);

        final OperationList opList=op.get("webParams", OperationList.class);
        assertNotNull(callType + "[Missing path parameters list]", opList);
        assertEquals(callType + "[Unexpected number of path parameters]", 1, opList.size());
        assertWebParams(callType, opList.get(0, OperationMap.class));
    }

    private void assertOperationValue (
            final String callType, final Operation op, final String valueType, final String key, final String expValue) {
        assertOperationValue(callType, op, valueType, key, expValue, String.class);
    }

    private <T> void assertOperationValue (
            final String callType, final Operation op, final String valueType,
            final String key, final T expValue, final Class<T> expType) {
        assertEquals(callType + "[Mismatched " + valueType + "]", expValue, op.get(key, expType));
    }

    private void assertWebParams (final String callType, final OperationMap map) {
        assertNotNull(callType + "[No web parameters map]", map);

        assertMapOperationValue(callType, map, "param name", "name", callType + JwsServiceDefinitions.PARAM_SUFFIX);
        assertMapOperationValue(callType, map, "param part", "partName", callType + JwsServiceDefinitions.PARAM_SUFFIX);
        assertMapOperationValue(callType, map, "param namespace", "targetNamespace", JwsServiceDefinitions.TARGET_NAMESPACE);
        assertMapOperationValue(callType, map, "param mode", "mode", WebParam.Mode.IN.toString());
        assertMapOperationValue(callType, map, "param header", "header", Boolean.valueOf(JwsServiceDefinitions.HEADER_PARAM), Boolean.class);
    }

    private void assertMapOperationValue (
            final String callType, final OperationMap map, final String valueType,
            final String key, final String expValue) {
        assertMapOperationValue(callType, map, valueType, key, expValue, String.class);
    }

    private  <T> void assertMapOperationValue (
            final String callType, final OperationMap map, final String valueType,
            final String key, final T expValue, final Class<T> expType) {
        assertEquals(callType + "[Mismatched " + valueType + "]", expValue, map.get(key, expType));
    }
}
