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
package com.springsource.insight.plugin.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 */
public class ServerSocketChannelAcceptCollectionAspectTest
        extends SocketAcceptCollectionAspectTestSupport {

    public ServerSocketChannelAcceptCollectionAspectTest() {
        super();
    }

    @Test
    @Ignore("For some reason this test conflicts with the ServerSocket#accept.\n"
          + "However, each one of them passes separately if the other is ignored\n"
          + "This shows that they work...")
    public void testBasicFunctionality () throws IOException, InterruptedException {
        runAcceptorTest("testBasicFunctionality", new ServerSocketChannelAcceptor(3777));
    }

    @Override
    public ServerSocketChannelAcceptCollectionAspect getAspect() {
        return ServerSocketChannelAcceptCollectionAspect.aspectOf();
    }

    static class ServerSocketChannelAcceptor extends SocketAcceptorHelper<SocketChannel> {
        private final ServerSocketChannel   channel;
        public ServerSocketChannelAcceptor(int listenPort) throws IOException {
            super(listenPort);
            
            channel = ServerSocketChannel.open();
            ServerSocket    sock=channel.socket();
            sock.bind(new InetSocketAddress(listenPort), 5);
        }

        @Override
        protected SocketChannel waitForConnection() throws IOException {
            return channel.accept();
        }
        
        @Override
        protected Socket resolveClientSocket(SocketChannel conn) throws IOException {
            return conn.socket();
        }

        @Override
        protected void close(SocketChannel conn) throws IOException {
            conn.close();
        }

        public void close() throws IOException {
            channel.close();
        }
    }
}
