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
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.Test;

/**
 *
 */
public class SocketAcceptCollectionAspectTest
        extends SocketAcceptCollectionAspectTestSupport {

    public SocketAcceptCollectionAspectTest() {
        super();
    }

    @Test
    public void testBasicFunctionality() throws IOException, InterruptedException {
        runAcceptorTest("testBasicFunctionality", new ServerSocketAcceptor(7365));
    }

    @Override
    public SocketAcceptCollectionAspect getAspect() {
        return SocketAcceptCollectionAspect.aspectOf();
    }

    static class ServerSocketAcceptor extends SocketAcceptorHelper<Socket> {
        private final ServerSocket socket;

        public ServerSocketAcceptor(int port) throws IOException {
            super(port);
            socket = new ServerSocket(port, 5);
        }

        @Override
        protected Socket waitForConnection() throws IOException {
            return socket.accept();
        }

        @Override
        protected Socket resolveClientSocket(Socket conn) throws IOException {
            return conn;
        }

        @Override
        protected void close(Socket conn) throws IOException {
            conn.close();
        }

        public void close() throws IOException {
            socket.close();
        }
    }
}
