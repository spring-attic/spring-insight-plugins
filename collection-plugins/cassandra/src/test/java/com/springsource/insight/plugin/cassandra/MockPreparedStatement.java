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
package com.springsource.insight.plugin.cassandra;


import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.RetryPolicy;

import java.nio.ByteBuffer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockPreparedStatement  implements PreparedStatement {

    public ColumnDefinitions getVariables() {
        return null;
    }

    public BoundStatement bind(Object... values) {
        BoundStatement bound =  mock(BoundStatement.class);
        when(bound.preparedStatement()).thenReturn(this);
        return bound;
    }

    public BoundStatement bind() {
        BoundStatement bound =  mock(BoundStatement.class);
        when(bound.preparedStatement()).thenReturn(this);
        when(bound.getKeyspace()).thenReturn("LoggedKeyspace");
        return bound;
    }

    public PreparedStatement setRoutingKey(ByteBuffer routingKey) {
        return null;
    }

    public PreparedStatement setRoutingKey(ByteBuffer... routingKeyComponents) {
        return null;
    }

    public ByteBuffer getRoutingKey() {
        return null;
    }

    public PreparedStatement setConsistencyLevel(ConsistencyLevel consistency) {
        return null;
    }

    public ConsistencyLevel getConsistencyLevel() {
        return null;
    }

    public PreparedStatement setSerialConsistencyLevel(ConsistencyLevel serialConsistency) {
        return null;
    }

    public ConsistencyLevel getSerialConsistencyLevel() {
        return null;
    }

    public String getQueryString() {
        return "SELECT * FROM keyspace.table";
    }

    public String getQueryKeyspace() {
        return null;
    }

    public PreparedStatement enableTracing() {
        return null;
    }

    public PreparedStatement disableTracing() {
        return null;
    }

    public boolean isTracing() {
        return false;
    }

    public PreparedStatement setRetryPolicy(RetryPolicy policy) {
        return null;
    }

    public RetryPolicy getRetryPolicy() {
        return null;
    }

    public PreparedId getPreparedId() {
        return null;
    }
}