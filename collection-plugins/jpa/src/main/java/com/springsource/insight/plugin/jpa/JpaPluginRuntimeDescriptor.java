package com.springsource.insight.plugin.jpa;

import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;

public class JpaPluginRuntimeDescriptor extends PluginRuntimeDescriptor {
    public static final String PLUGIN_NAME = "jpa";
    
    @Override
    public EndPointAnalyzer[] getEndPointAnalyzers() {
        return null;
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

}
