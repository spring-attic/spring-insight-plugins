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

import java.net.ServerSocket;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;

/**
 *
 */
public abstract aspect SocketAcceptCollectionAspectSupport extends SocketOperationCollectionAspectSupport {
    protected SocketAcceptCollectionAspectSupport() {
        setCollector(new SocketAcceptOperationCollector(getSocketCollectOperationContext()));
    }

    @Override
    protected Operation createOperation(JoinPoint jp) {
        ServerSocket sock = resolveServerSocket(jp);
        /*
         * NOTE: we use "0.0.0.0" as a placeholder address since at this stage
         * we cannot tell what is the client's address since the method is
         * called by the <code>before</code> advice. The SocketAcceptOperationCollector
         * will fill this in
         */
        return createOperation(super.createOperation(jp), SocketDefinitions.ACCEPT_ACTION, "0.0.0.0", sock.getLocalPort());
    }

    protected abstract ServerSocket resolveServerSocket(JoinPoint jp);

    @Override
    public String getPluginName() {
        return SocketPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
