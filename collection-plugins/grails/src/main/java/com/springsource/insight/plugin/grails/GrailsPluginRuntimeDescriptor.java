package com.springsource.insight.plugin.grails;

import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;

public class GrailsPluginRuntimeDescriptor extends PluginRuntimeDescriptor {

    @Override
    public EndPointAnalyzer[] getEndPointAnalyzers() {
        return toArray(new GrailsControllerMethodEndPointAnalyzer());
    }

}
