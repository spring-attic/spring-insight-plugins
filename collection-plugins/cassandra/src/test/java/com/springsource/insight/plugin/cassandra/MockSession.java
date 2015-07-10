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
package com.springsource.insight.plugin.cassandra;


import com.datastax.driver.core.*;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockSession implements Session {

    public String getLoggedKeyspace() {
        return "LoggedKeyspace";
    }

    public Session init() {
        return null;
    }

    public ResultSet execute(String query) {
        SimpleStatement simple = new SimpleStatement(query);
        simple.setKeyspace(getLoggedKeyspace());
        return execute(simple);
    }

    public ResultSet execute(String query, Object... values) {
        SimpleStatement simple = new SimpleStatement(query,values);
        simple.setKeyspace(getLoggedKeyspace());
        return execute(simple);
    }

    public ResultSet execute(Statement statement) {
        return executeAsync(statement).getUninterruptibly();
    }

    public ResultSetFuture executeAsync(String query) {
        SimpleStatement simple = new SimpleStatement(query);
        simple.setKeyspace(getLoggedKeyspace());
        return executeAsync(simple);
    }

    public ResultSetFuture executeAsync(String query, Object... values) {
        SimpleStatement simple = new SimpleStatement(query,values);
        simple.setKeyspace(getLoggedKeyspace());
        return executeAsync(simple);
    }

    public ResultSetFuture executeAsync(Statement statement) {
        return new MockResultSetFuture();
    }

    public PreparedStatement prepare(String query) {
        return null;
    }

    public PreparedStatement prepare(RegularStatement statement) {
        return null;
    }

    public ListenableFuture<PreparedStatement> prepareAsync(String query) {
        return null;
    }

    public ListenableFuture<PreparedStatement> prepareAsync(RegularStatement statement) {
        return null;
    }

    public CloseFuture closeAsync() {
        return null;
    }

    public void close() {

    }

    public boolean isClosed() {
        return false;
    }

    public Cluster getCluster() {

        Cluster cluster = mock(Cluster.class);
        Metadata metadata = mock(Metadata.class);
        Configuration configuration = mock(Configuration.class);
        ProtocolOptions protocolOptions = mock(ProtocolOptions.class);

        when(metadata.getClusterName()).thenReturn("my_cluster");
        when(metadata.getAllHosts()).thenReturn(Collections.EMPTY_SET);
        when(cluster.getMetadata()).thenReturn(metadata);
        when(cluster.getConfiguration()).thenReturn(configuration);
        when(configuration.getProtocolOptions()).thenReturn(protocolOptions);
        when(protocolOptions.getPort()).thenReturn(1234);

        return cluster;
    }


    public State getState() {
        return null;
    }
}
