package com.springsource.insight.plugin.methodEndPoint;

import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;

public class EndPointPluginRuntimeDescriptor extends PluginRuntimeDescriptor {

    @Override
    public EndPointAnalyzer[] getEndPointAnalyzers() {
        return toArray(new TopLevelMethodEndPointAnalyzer());
    }

}
