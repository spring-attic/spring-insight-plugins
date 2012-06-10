package com.springsource.insight.plugin.springweb;

import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;
import com.springsource.insight.plugin.springweb.controller.ControllerEndPointAnalyzer;

public class SpringWebPluginRuntimeDescriptor extends PluginRuntimeDescriptor {

    public static final String PLUGIN_NAME = "spring-web";

    @Override
    public EndPointAnalyzer[] getEndPointAnalyzers() {
        return toArray(new ControllerEndPointAnalyzer());
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

}
