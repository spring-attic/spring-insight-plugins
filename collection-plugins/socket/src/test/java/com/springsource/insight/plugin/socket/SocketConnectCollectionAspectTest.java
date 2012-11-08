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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import org.junit.Test;

import com.springsource.insight.collection.ObscuredValueSetMarker;


/**
 * Note: the basic functionality is tested (indirectly) by {@link SocketExternalResourceAnalyzerTest}
 * so we don't repeat it here
 */
public class SocketConnectCollectionAspectTest
       extends SocketOperationCollectionAspectTestSupport {
    public SocketConnectCollectionAspectTest() {
        super();
    }

	@Test
    public void testMatchingObscuredAddressesPattern () {
        runObscuredTest("^10\\..*", "10.1.2.3", true);
    }

    @Test
    public void testNonMatchingObscuredAddressesPattern () {
        runObscuredTest("^10\\..*", "192.168.3.1", false);
    }

    private void runObscuredTest (String pattern, String testAddress, boolean shouldObscure) {
    	ObscuredValueSetMarker    marker=
                setupObscuredTest(SocketCollectOperationContext.OBSCURED_ADDRESSES_PATTERN_SETTING, pattern);
        SocketAddress         	  connectAddress=new InetSocketAddress(testAddress, TEST_PORT);
        Socket                    socket=new Socket();
        try {
            socket.connect(connectAddress, 125);
            socket.close(); // just in case it somehow succeeded
        } catch(IOException e) {
            // ignored since we don't really expect it to succeed
        }

        assertSocketOperation(SocketDefinitions.CONNECT_ACTION, testAddress, TEST_PORT);
        assertObscureTestResults(marker, pattern, testAddress, shouldObscure);
    }

    @Override
    public SocketConnectCollectionAspect getAspect() {
        return SocketConnectCollectionAspect.aspectOf();
    }
}
