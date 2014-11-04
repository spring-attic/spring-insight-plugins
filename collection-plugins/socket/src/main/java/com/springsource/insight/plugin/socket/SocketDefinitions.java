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
package com.springsource.insight.plugin.socket;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;

/**
 *
 */
public final class SocketDefinitions {

    private SocketDefinitions() {
        // no instance
    }

    public static final OperationType TYPE = OperationType.valueOf("socket");
    // used operation attributes
    public static final String ACTION_ATTR = "action",
            CONNECT_ACTION = "connect",
            ACCEPT_ACTION = "accept",
            CLOSE_ACTION = "close",
            SHUTDOWN_ACTION = "shutdown",
            ADDRESS_ATTR = "address",
            ANY_ADDRESS = "0.0.0.0",
            PORT_ATTR = "port";

    static Operation initializeOperation(Operation op, String action, String addr, int port) {
        op.type(TYPE)
                .put(ACTION_ATTR, action)
                .put(ADDRESS_ATTR, addr)
                .put(PORT_ATTR, port)
        ;
        op.label(label(op));
        return op;
    }

    static String label(Operation op) {
        return op.get(ACTION_ATTR, String.class)
                + " " + op.get(ADDRESS_ATTR, String.class)
                + ":" + op.get(PORT_ATTR, Integer.class)
                ;
    }

    static String resolveConnectAddress(SocketAddress sockAddr) {
        String host = null;
        if (sockAddr instanceof InetSocketAddress) {
            InetSocketAddress inetAddr = (InetSocketAddress) sockAddr;
            InetAddress addr = inetAddr.getAddress();
            // can happen if failed to resolve original host
            host = (addr == null) ? null : addr.getHostAddress();
            if ((host == null) || (host.length() <= 0)) {
                host = inetAddr.getHostName();
            }
        }

        if ((host == null) || (host.length() <= 0)) {
            return ANY_ADDRESS;
        }

        return host;
    }

    static int resolveConnectPort(SocketAddress sockAddr) {
        if (sockAddr instanceof InetSocketAddress) {
            return ((InetSocketAddress) sockAddr).getPort();
        }

        return (-1);
    }
}
