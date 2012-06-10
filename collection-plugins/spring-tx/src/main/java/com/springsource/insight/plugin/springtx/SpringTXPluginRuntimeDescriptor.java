package com.springsource.insight.plugin.springtx;

import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;

public class SpringTXPluginRuntimeDescriptor extends PluginRuntimeDescriptor {
    public static final String PLUGIN_NAME = "spring-tx";
    
    @Override
    public EndPointAnalyzer[] getEndPointAnalyzers() {
        return null;
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

}
