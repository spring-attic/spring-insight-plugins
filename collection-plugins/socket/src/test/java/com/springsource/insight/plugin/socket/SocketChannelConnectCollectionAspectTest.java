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
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

import org.junit.Assert;
import org.junit.Test;


/**
 * 
 */
public class SocketChannelConnectCollectionAspectTest
        extends SocketConnectCollectionAspectTestSupport {
    public SocketChannelConnectCollectionAspectTest() {
        super();
    }

    @Test
    public void testStaticChannelOpen () {
        final SocketAddress connectAddress=new InetSocketAddress(TEST_HOST, TEST_PORT);
        try {
            SocketChannel channel=SocketChannel.open(connectAddress);
            Assert.fail("Unexpected success to connect to " + connectAddress);
            channel.close();
        } catch(IOException e) {
            // ignored since we don't really expect it to succeed
        }
        
        assertConnectOperation(connectAddress);
    }

    @Test
    public void testChannelConnectMethod () throws IOException {
        final SocketAddress connectAddress=new InetSocketAddress(TEST_HOST, TEST_PORT);
        SocketChannel channel=SocketChannel.open();
        try {
            if (channel.connect(connectAddress))
                Assert.fail("Unexpected success to connect to " + connectAddress);
        } catch(IOException e) {
            // ignored since we don't really expect it to succeed
        } finally {
            channel.close();
        }
        
        assertConnectOperation(connectAddress);
    }

    @Override
    public SocketChannelConnectCollectionAspect getAspect() {
        return SocketChannelConnectCollectionAspect.aspectOf();
    }

}
