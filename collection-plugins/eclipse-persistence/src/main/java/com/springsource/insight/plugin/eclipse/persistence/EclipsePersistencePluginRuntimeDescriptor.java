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

package com.springsource.insight.plugin.eclipse.persistence;

import java.util.Collection;
import java.util.List;

import com.springsource.insight.intercept.metrics.MetricsGenerator;
import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;
import com.springsource.insight.util.ArrayUtil;

public class EclipsePersistencePluginRuntimeDescriptor extends PluginRuntimeDescriptor {
    private static final EclipsePersistencePluginRuntimeDescriptor INSTANCE = new EclipsePersistencePluginRuntimeDescriptor();
    private static final List<? extends MetricsGenerator> mGenerators =
            ArrayUtil.asUnmodifiableList(DatabaseSessionMetricsGenerator.getInstance(),
                    SessionQueryMetricsGenerator.getInstance(),
                    TransactionOperationMetricsGenerator.getInstance());

    private EclipsePersistencePluginRuntimeDescriptor() {
        super();
    }

    public static final EclipsePersistencePluginRuntimeDescriptor getInstance() {
        return INSTANCE;
    }

    @Override
    public Collection<? extends MetricsGenerator> getMetricsGenerators() {
        return mGenerators;
    }

    @Override
    public String getPluginName() {
        return EclipsePersistenceDefinitions.PLUGIN_NAME;
    }
}
