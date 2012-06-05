package com.springsource.insight.plugin.springbatch;

import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;

public class SpringBatchPluginRuntimeDescriptor extends PluginRuntimeDescriptor {

    @Override
    public EndPointAnalyzer[] getEndPointAnalyzers() {
        return toArray(new SpringBatchEndPointAnalyzer());
    }

}
