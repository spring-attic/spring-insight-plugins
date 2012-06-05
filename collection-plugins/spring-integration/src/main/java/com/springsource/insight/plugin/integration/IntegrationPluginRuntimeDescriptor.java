package com.springsource.insight.plugin.integration;

import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;

public class IntegrationPluginRuntimeDescriptor extends PluginRuntimeDescriptor {

    @Override
    public EndPointAnalyzer[] getEndPointAnalyzers() {
        return toArray(new IntegrationEndPointAnalyzer());
    }

}
