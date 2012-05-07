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

import java.net.SocketAddress;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public abstract aspect SocketConnectCollectionAspectSupport extends SocketOperationCollectionAspectSupport {
    protected SocketConnectCollectionAspectSupport () {
        super();
    }

    @Override
    protected Operation createOperation(JoinPoint jp) {
        SocketAddress   sockAddr=resolveConnectAddress(jp);
        String          addr=SocketDefinitions.resolveConnectAddress(sockAddr);
        int             port=SocketDefinitions.resolveConnectPort(sockAddr);
        return createOperation(super.createOperation(jp), SocketDefinitions.CONNECT_ACTION, addr, port);
    }

    protected abstract SocketAddress resolveConnectAddress (JoinPoint jp);

    @Override
    public String getPluginName() {
        return "socket";
    }
}
