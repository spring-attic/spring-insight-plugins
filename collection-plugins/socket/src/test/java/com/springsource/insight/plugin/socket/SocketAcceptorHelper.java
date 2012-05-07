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

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public abstract class SocketAcceptorHelper<T> implements Runnable, Closeable {
    private Operation       operation;
    private IOException     exception;
    private final int       port;
    protected final Logger    logger=Logger.getLogger(getClass().getName());
    protected SocketAcceptorHelper(final int listenPort) {
        port = listenPort;
    }

    public int getListenPort () {
        return port;
    }

    public Operation getOperation () {
        return operation;
    }

    public IOException getException () {
        return exception;
    }

    public void run() {
        try {
            logger.info("Start wait on " + getListenPort());
            T  connection=waitForConnection();
            try {
                Socket      sock=resolveClientSocket(connection);
                InetAddress addr=sock.getInetAddress();
                String      addrValue=addr.getHostAddress();
                logger.info("Accepted connection from " + addrValue);
                operation = SocketDefinitions.initializeOperation(
                                new Operation(), SocketDefinitions.ACCEPT_ACTION, addrValue, getListenPort());
            } finally {
                close(connection);
            }
        } catch(IOException e) {
            exception = e;
        }
    }

    protected abstract void close (T conn) throws IOException;

    protected abstract Socket resolveClientSocket (T conn) throws IOException;

    protected abstract T waitForConnection () throws IOException;
}
