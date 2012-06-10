package com.springsource.insight.plugin.spring.security;

import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;

public class SpringSecurityPluginRuntimeDescriptor extends PluginRuntimeDescriptor {

    public static final String PLUGIN_NAME = "spring-security";

    @Override
    public EndPointAnalyzer[] getEndPointAnalyzers() {
        return null;
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

}
