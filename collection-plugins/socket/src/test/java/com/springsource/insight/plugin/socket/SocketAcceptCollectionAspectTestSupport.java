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
package com.springsource.insight.plugin.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import com.springsource.insight.collection.test.AbstractCollectionTestSupport;
import com.springsource.insight.intercept.operation.Operation;


/**
 * Note: we do not inherit from OperationCollectionAspectTestSupport due
 * to connect/accept aspects conflicts
 */
public abstract class SocketAcceptCollectionAspectTestSupport extends AbstractCollectionTestSupport {

    protected SocketAcceptCollectionAspectTestSupport() {
        super();
    }

    protected void runAcceptorTest(String testName, SocketAcceptorHelper<?> acceptor) throws IOException, InterruptedException {
        SocketOperationCollectionAspectSupport aspectInstance = getAspect();
        SocketCollectOperationContext context = aspectInstance.getSocketCollectOperationContext();
        TestCollector collector = new TestCollector(context);
        aspectInstance.setCollector(collector);

        Thread t = new Thread(acceptor, "t" + testName);
        t.start();
        try {
            SocketAddress connectAddress = new InetSocketAddress("localhost", acceptor.getListenPort());
            Socket socket = new Socket();
            socket.connect(connectAddress, 125);
            socket.close(); // just in case it somehow succeeded

            t.join(TimeUnit.SECONDS.toMillis(5L));
            assertFalse(testName + ": Accepting thread still alive", t.isAlive());

            Operation opCollected = collector.getCollectedOperation();
            assertNotNull(testName + ": No operation collected", opCollected);
            assertEquals(testName + ": Mismatched types", SocketDefinitions.TYPE, opCollected.getType());

            Operation opAccepted = acceptor.getOperation();
            assertNotNull(testName + ": No operation accepted", opAccepted);

            for (String attrName : new String[]{
                    SocketDefinitions.ACTION_ATTR,
                    SocketDefinitions.ADDRESS_ATTR,
                    SocketDefinitions.PORT_ATTR
            }) {
                Object valCollected = opCollected.get(attrName),
                        valAccepted = opAccepted.get(attrName);
                assertEquals(testName + ": Mismatched values for " + attrName, valCollected, valAccepted);
            }

        } finally {
            acceptor.close();
        }
    }

    public abstract SocketOperationCollectionAspectSupport getAspect();

    /**
     * A &quot;poor-man's&quot; replacement for argument-captor since we cannot
     * use it due to conflicts between the connect and accept aspects which are
     * invoked both
     */
    static class TestCollector extends SocketAcceptOperationCollector {
        private volatile Operation collectedOperation;

        public TestCollector(SocketCollectOperationContext context) {
            super(context);
        }

        public Operation getCollectedOperation() {
            if ((collectedOperation != null) && collectedOperation.isFinalizable()) {
                collectedOperation.finalizeConstruction();
            }

            return collectedOperation;
        }

        @Override
        protected void processNormalExit(Operation op, Object returnValue) {
            if (!SocketDefinitions.ACCEPT_ACTION.equals(op.get(SocketDefinitions.ACTION_ATTR, String.class))) {
                return; // ignore non-accept operations
            }

            if (collectedOperation != null) {
                throw new IllegalStateException("Multiple calls to processNormalExit");
            }
            super.processNormalExit(op, returnValue);
            collectedOperation = op;
        }

    }
}
