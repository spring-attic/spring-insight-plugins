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
import java.net.Socket;
import java.net.SocketAddress;

import org.junit.Assert;
import org.junit.Test;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.topology.ExternalResourceType;

/**
 * 
 */
public class SocketExternalResourceAnalyzerTest
        extends SocketConnectCollectionAspectTestSupport {

    public SocketExternalResourceAnalyzerTest() {
        super();
    }

    // NOTE: it also indirectly tests the SocketConnectCollectionAspect
    //      so no need for specific tests for it
    @Test
    public void testSocketExternalResourceAnalyzer () {
        final SocketAddress connectAddress=new InetSocketAddress(TEST_HOST, TEST_PORT);
        final Socket        socket=new Socket();
        try {
            socket.connect(connectAddress, 125);
            Assert.fail("Unexpected success to connect to " + connectAddress);
        } catch(IOException e) {
            // ignored since we don't really expect it to succeed
        }

        Operation   op=assertConnectOperation(connectAddress);
        runExternalResourceAnalyzer(op, ExternalResourceType.SERVER, op.get(SocketDefinitions.ADDRESS_ATTR, String.class), TEST_PORT);
    }

    @Override
    public SocketConnectCollectionAspect getAspect() {
        return SocketConnectCollectionAspect.aspectOf();
    }
}
