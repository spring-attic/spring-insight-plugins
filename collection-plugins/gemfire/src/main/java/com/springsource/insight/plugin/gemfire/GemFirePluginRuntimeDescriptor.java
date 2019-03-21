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
package com.springsource.insight.plugin.gemfire;

import java.util.Collection;
import java.util.List;

import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceAnalyzer;
import com.springsource.insight.util.ArrayUtil;

public class GemFirePluginRuntimeDescriptor extends PluginRuntimeDescriptor {
    public static final String PLUGIN_NAME = "gemfire";
    private static final GemFirePluginRuntimeDescriptor INSTANCE = new GemFirePluginRuntimeDescriptor();
    private static final List<? extends ExternalResourceAnalyzer> extResAnalyzers =
            ArrayUtil.asUnmodifiableList(GemFireQueryExternalResourceAnalyzer.getInstance(),
                    GemFireRemoteExternalResourceAnalyzer.getInstance());

    private GemFirePluginRuntimeDescriptor() {
        super();
    }

    public static final GemFirePluginRuntimeDescriptor getInstance() {
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
