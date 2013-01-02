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
package com.springsource.insight.plugin.jdbc;

import java.util.Collection;

import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;

/**
 * Creates DB external resources and if needed will also create a QUERY external
 * resources as children
 */
public class JdbcOperationExternalResourceAnalyzer extends DatabaseJDBCURIAnalyzer {
    public static final OperationType TYPE = OperationType.valueOf("jdbc");

    private static final JdbcOperationExternalResourceAnalyzer INSTANCE = new JdbcOperationExternalResourceAnalyzer();

    private JdbcQueryExternalResourceGenerator queryExternalResourceGenerator;

    private JdbcOperationExternalResourceAnalyzer() {
        this(TYPE, JdbcQueryExternalResourceGenerator.getInstance());
    }

    // package visibility for unit tests
    JdbcOperationExternalResourceAnalyzer(OperationType type, JdbcQueryExternalResourceGenerator generator) {
        super(type);
        this.queryExternalResourceGenerator = generator;
    }

    public static final JdbcOperationExternalResourceAnalyzer getInstance() {
        return INSTANCE;
    }

    @Override
    public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace, Collection<Frame> dbFrames) {
        return queryExternalResourceGenerator
                .createAndAddQueryExternalResourceDescriptors(
                        super.locateExternalResourceName(trace, dbFrames),
                        trace);
    }
}
