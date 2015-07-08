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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceAnalyzer;
import com.springsource.insight.intercept.trace.TraceErrorAnalyzer;
import com.springsource.insight.util.ArrayUtil;

public class CassandraPluginRuntimeDescriptor extends PluginRuntimeDescriptor {
    public static final String PLUGIN_NAME = "cassandra12";
    private static final CassandraPluginRuntimeDescriptor INSTANCE = new CassandraPluginRuntimeDescriptor();
    private static final List<? extends ExternalResourceAnalyzer> extResAnalyzers =
            ArrayUtil.asUnmodifiableList(CassandraConnectExternalResourceAnalyzer.getInstance(),
                    CassandraCQLExternalResourceAnalyzer.getInstance(),
                    CassandraGetExternalResourceAnalyzer.getInstance(),
                    CassandraRemoveExternalResourceAnalyzer.getInstance(),
                    CassandraSystemExternalResourceAnalyzer.getInstance(),
                    CassandraUpdateExternalResourceAnalyzer.getInstance());

    private CassandraPluginRuntimeDescriptor() {
        super();
    }

    public static final CassandraPluginRuntimeDescriptor getInstance() {
        return INSTANCE;
    }

    @Override
    public Collection<? extends ExternalResourceAnalyzer> getExternalResourceAnalyzers() {
        return extResAnalyzers;
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }
}
