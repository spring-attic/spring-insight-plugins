package com.springsource.insight.plugin.springbatch;

import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;

public class SpringBatchPluginRuntimeDescriptor extends PluginRuntimeDescriptor {
    public static final String PLUGIN_NAME = "spring-batch";
    
    @Override
    public EndPointAnalyzer[] getEndPointAnalyzers() {
        return toArray(new SpringBatchEndPointAnalyzer());
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

}
