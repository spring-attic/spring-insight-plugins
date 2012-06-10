package com.springsource.insight.plugin.eclipse.persistence;

import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;

public class EclipsePersistencePluginRuntimeDescriptor extends PluginRuntimeDescriptor {

    @Override
    public EndPointAnalyzer[] getEndPointAnalyzers() {
        return null;
    }

    @Override
    public String getPluginName() {
        return EclipsePersistenceDefinitions.PLUGIN_NAME;
    }

}
