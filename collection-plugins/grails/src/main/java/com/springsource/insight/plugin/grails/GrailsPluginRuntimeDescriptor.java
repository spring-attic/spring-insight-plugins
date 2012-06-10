package com.springsource.insight.plugin.grails;

import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;

public class GrailsPluginRuntimeDescriptor extends PluginRuntimeDescriptor {
    public static final String PLUGIN_NAME = "grails";
    
    @Override
    public EndPointAnalyzer[] getEndPointAnalyzers() {
        return toArray(new GrailsControllerMethodEndPointAnalyzer());
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

}
