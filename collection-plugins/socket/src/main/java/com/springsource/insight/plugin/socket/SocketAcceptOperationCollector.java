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
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.DefaultOperationCollector;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.logging.InsightLogManager;
import com.springsource.insight.util.logging.InsightLogger;

/**
 * A &quot;replacement&quot; for the {@link OperationCollector} used by the
 * aspects by default since we want to extract the client's address - which
 * we can do only <U>after</U> the operation has been completed - which is
 * not the case when {@link SocketAcceptCollectionAspect#createOperation(JoinPoint)}
 * is called
 */
public class SocketAcceptOperationCollector extends DefaultOperationCollector {
    private final SocketCollectOperationContext collectionContext;
    private final InsightLogger logger = InsightLogManager.getLogger(getClass().getName());

    public SocketAcceptOperationCollector(SocketCollectOperationContext context) {
        collectionContext = context;
    }

    @Override
    protected void processNormalExit(Operation op, Object returnValue) {
        Socket sock = resolveAcceptedSocket(returnValue);
        InetAddress addr = sock.getInetAddress();
        String addrValue = addr.getHostAddress();
        // replace the placeholder from createOperation with the actual value
        op.put(SocketDefinitions.ADDRESS_ATTR, addrValue);
        // update the label since address changed
        op.label(SocketDefinitions.label(op));

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("processNormalExit(" + op.getLabel() + ")");
        }

        if (collectionContext.updateObscuredAddressValue(addrValue)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("processNormalExit(" + op.getLabel() + ") obscured");
            }
        }
    }

    protected Socket resolveAcceptedSocket(Object returnValue) {
        if (returnValue instanceof Socket) {
            return (Socket) returnValue;
        } else if (returnValue instanceof SocketChannel) {  // if accepted via a ServerSocketChannel
            return ((SocketChannel) returnValue).socket();
        } else {
            throw new NoSuchElementException("Unknown accepted return value type: " + returnValue);
        }
    }
}
