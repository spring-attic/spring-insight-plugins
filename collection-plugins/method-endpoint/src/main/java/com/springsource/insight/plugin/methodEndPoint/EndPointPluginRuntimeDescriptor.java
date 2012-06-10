package com.springsource.insight.plugin.methodEndPoint;

import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;

public class EndPointPluginRuntimeDescriptor extends PluginRuntimeDescriptor {
    public static final String PLUGIN_NAME = "method-endpoint";
    
    @Override
    public EndPointAnalyzer[] getEndPointAnalyzers() {
        return toArray(new TopLevelMethodEndPointAnalyzer());
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

}
