package com.springsource.insight.plugin.integration;

import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;

public class IntegrationPluginRuntimeDescriptor extends PluginRuntimeDescriptor {
    public static final String PLUGIN_NAME = "spring-integration";
    
    @Override
    public EndPointAnalyzer[] getEndPointAnalyzers() {
        return toArray(new IntegrationEndPointAnalyzer());
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

}
