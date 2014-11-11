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

package com.springsource.insight.plugin.integration.tcp;

import org.springframework.integration.ip.tcp.connection.TcpConnection;

import com.springsource.insight.collection.DefaultOperationCollector;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;

/**
 *
 */
public class TcpConnectionOperationCollector extends DefaultOperationCollector {
    public static final String HOST_ADDRESS_ATTR = "hostAddress",
            PORT_ATTR = "port",
            CONNID_ATTR = "connectionId",
            SERVER_ATTR = "serverMode";

    public TcpConnectionOperationCollector() {
        super();
    }

    @Override
    protected void processNormalExit(Operation op, Object returnValue) {
        if (!(returnValue instanceof TcpConnection)) {
            return; // not expecting anything else, but let's no belabor the
            // point...
        }

        TcpConnection conn = (TcpConnection) returnValue;
        String host = conn.getHostAddress();
        int port = conn.getPort();
        op.put(HOST_ADDRESS_ATTR, host)
                .put(PORT_ATTR, port)
                .put(OperationFields.URI, "tcp://" + host + ":" + port)
                .putAnyNonEmpty(CONNID_ATTR, conn.getConnectionId())
                .put(SERVER_ATTR, conn.isServer());
    }
}
