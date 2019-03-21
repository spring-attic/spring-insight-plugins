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


import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;

import java.util.Date;

public class MockBoundStatement extends BoundStatement {

    public MockBoundStatement(PreparedStatement statement) {
        super(statement);
    }
    @Override
    public BoundStatement bind(Object... values) {
        return this;
    }

    @Override
    public BoundStatement setBool(int index, boolean b) {
        return this;

    }
    @Override
    public BoundStatement setDouble(int index, double b) {
        return this;

    }
    @Override
    public BoundStatement setDate(int index, Date b) {
        return this;

    }


    @Override
    public BoundStatement setBool(String key, boolean b) {
        return this;

    }
    @Override
    public BoundStatement setDouble(String key, double b) {
        return this;

    }
    @Override
    public BoundStatement setDate(String key, Date b) {
        return this;

    }

}

