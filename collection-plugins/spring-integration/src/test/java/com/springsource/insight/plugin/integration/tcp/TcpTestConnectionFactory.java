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

package com.springsource.insight.plugin.integration.tcp;

import java.util.UUID;

import org.springframework.core.serializer.DefaultDeserializer;
import org.springframework.core.serializer.DefaultSerializer;
import org.springframework.core.serializer.Deserializer;
import org.springframework.core.serializer.Serializer;
import org.springframework.messaging.Message;
import org.springframework.integration.ip.tcp.connection.ConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpConnection;
import org.springframework.integration.ip.tcp.connection.TcpListener;
import org.springframework.integration.ip.tcp.connection.TcpMessageMapper;
import org.springframework.integration.ip.tcp.connection.TcpSender;
import org.springframework.util.Assert;

/**
 *
 */
public class TcpTestConnectionFactory implements ConnectionFactory, TcpConnection {
    private final String host;
    private final int port;
    private final boolean server;
    private final String connId = UUID.randomUUID().toString();
    private boolean running, open = true, single;
    private Deserializer<?> deser = new DefaultDeserializer();
    private Serializer<?> ser = new DefaultSerializer();

    /**
     *
     */
    public TcpTestConnectionFactory(String hostname, int portValue, boolean asServer) {
        host = hostname;
        port = portValue;
        server = asServer;
    }

    public boolean isAutoStartup() {
        return false;
    }

    public void stop(Runnable callback) {
        callback.run();
        running = false;
    }

    public void start() {
        running = true;
    }

    public void stop() {
        stop(this);
    }

    public boolean isRunning() {
        return running;
    }

    public int getPhase() {
        return (-1);
    }

    public void run() {
        // ignored
    }

    public void close() {
        stop();
        open = false;
    }

    public boolean isOpen() {
        return open;
    }

    public void send(Message<?> message) throws Exception {
        throw new UnsupportedOperationException("send(" + message + ") N/A");
    }

    public Object getPayload() throws Exception {
        throw new UnsupportedOperationException("getPayload() N/A");
    }

    public String getHostName() {
        return host;
    }

    public String getHostAddress() {
        return getHostName();
    }

    public int getPort() {
        return port;
    }

    public void registerListener(TcpListener listener) {
        throw new UnsupportedOperationException("registerListener(" + listener + ") N/A");
    }

    public void registerSender(TcpSender sender) {
        throw new UnsupportedOperationException("registerSender(" + sender + ") N/A");
    }

    public String getConnectionId() {
        return connId;
    }

    public void setSingleUse(boolean singleUse) {
        single = singleUse;
    }

    public boolean isSingleUse() {
        return single;
    }

    public boolean isServer() {
        return server;
    }

    public void setMapper(TcpMessageMapper mapper) {
        throw new UnsupportedOperationException("setMapper(" + mapper + ") N/A");
    }

    public Deserializer<?> getDeserializer() {
        return deser;
    }

    public void setDeserializer(Deserializer<?> deserializer) {
        Assert.notNull(deserializer, "No deserializer");
        deser = deserializer;
    }

    public Serializer<?> getSerializer() {
        return ser;
    }

    public void setSerializer(Serializer<?> serializer) {
        Assert.notNull(serializer, "No serializer");
        ser = serializer;
    }

    public TcpListener getListener() {
        return null;
    }

    public long incrementAndGetConnectionSequence() {
        return 0L;
    }

    public Object getDeserializerStateKey() {
        //do nothing
        return null;
    }

    public TcpConnection getConnection() throws Exception {
        return this;
    }
}
