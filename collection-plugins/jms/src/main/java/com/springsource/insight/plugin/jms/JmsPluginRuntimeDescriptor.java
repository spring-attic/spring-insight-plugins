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
package com.springsource.insight.plugin.jms;

import java.util.Collection;
import java.util.List;

import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.metrics.MetricsGenerator;
import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceAnalyzer;
import com.springsource.insight.intercept.trace.TraceSourceAnalyzer;
import com.springsource.insight.util.ArrayUtil;

public class JmsPluginRuntimeDescriptor extends PluginRuntimeDescriptor {
    public static final String PLUGIN_NAME = "jms";
    private static final JmsPluginRuntimeDescriptor INSTANCE = new JmsPluginRuntimeDescriptor();
    private static final List<? extends ExternalResourceAnalyzer> extResAnalyzers =
            ArrayUtil.asUnmodifiableList(JMSConsumerResourceAnalyzer.getInstance(),
                    JMSMessageListenerResourceAnalyzer.getInstance(),
                    JMSProducerResourceAnalyzer.getInstance());
    private static final List<? extends TraceSourceAnalyzer> tsAnalyzers =
            ArrayUtil.asUnmodifiableList(JmsTraceSourceAnalyzer.getInstance());
    private static final List<? extends MetricsGenerator> mGenerators =
            ArrayUtil.asUnmodifiableList(JMSListenerReceiveMetricsGenerator.getInstance(),
                    JMSReceiveMetricsGenerator.getInstance(),
                    JMSSendMetricsGenerator.getInstance());
    private static final List<? extends EndPointAnalyzer> epAnalyzers =
            ArrayUtil.asUnmodifiableList(JMSEndPointAnalyzer.getInstance());

    private JmsPluginRuntimeDescriptor() {
        super();
    }

    public static final JmsPluginRuntimeDescriptor getInstance() {
        return INSTANCE;
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public Collection<? extends ExternalResourceAnalyzer> getExternalResourceAnalyzers() {
        return extResAnalyzers;
    }

    @Override
    public Collection<? extends TraceSourceAnalyzer> getTraceSourceAnalyzers() {
        return tsAnalyzers;
    }

    @Override
    public Collection<? extends MetricsGenerator> getMetricsGenerators() {
        return mGenerators;
    }

    @Override
    public Collection<? extends EndPointAnalyzer> getEndPointAnalyzers() {
        return epAnalyzers;
    }

}
