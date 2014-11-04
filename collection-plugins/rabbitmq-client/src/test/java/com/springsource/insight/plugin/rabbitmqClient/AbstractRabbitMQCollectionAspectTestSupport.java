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

package com.springsource.insight.plugin.rabbitmqClient;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.util.ArrayUtil;

/**
 *
 */
public abstract class AbstractRabbitMQCollectionAspectTestSupport
        extends OperationCollectionAspectTestSupport {

    protected final RabbitPluginOperationType pluginOpType;

    protected AbstractRabbitMQCollectionAspectTestSupport(RabbitPluginOperationType rabbitOpType) {
        if ((pluginOpType = rabbitOpType) == null) {
            throw new IllegalStateException("No plugin operation type specified");
        }
    }

    protected Operation assertBasicOperation(BasicProperties props, byte[] body, String opLabel) {
        return assertBasicOperation(assertOperationCreated(), props, body, opLabel);
    }

    protected Operation assertBasicOperation(Operation op, BasicProperties props, byte[] body, String opLabel) {
        assertEquals("Mismatched body length", ArrayUtil.length(body), op.getInt("bytes", (-1)));
        assertEquals("Mismatched label", opLabel, op.getLabel());

        for (String propName : new String[]{"connectionUrl", "serverVersion", "clientVersion"}) {
            assertNullValue(propName, op.get(propName));
        }

        OperationMap propsMap = op.get("props", OperationMap.class);
        assertNotNull("No properties extracted", propsMap);

        assertEquals("Mismatched application ID", props.getAppId(), propsMap.get("App Id"));
        assertEquals("Mismatched content encoding", props.getContentEncoding(), propsMap.get("Content Encoding"));
        assertEquals("Mismatched content type", props.getContentType(), propsMap.get("Content Type"));
        assertEquals("Mismatched delivery mode", props.getDeliveryMode().intValue(), propsMap.getInt("Delivery Mode", (-1)));
        assertEquals("Mismatched expiration", props.getExpiration(), propsMap.get("Expiration"));
        return op;
    }

    protected Operation assertOperationCreated() {
        Operation op = getLastEntered();
        assertNotNull("No operation entered", op);
        assertEquals("Mismatched operation type", pluginOpType.getOperationType(), op.getType());
        return op;
    }
}
