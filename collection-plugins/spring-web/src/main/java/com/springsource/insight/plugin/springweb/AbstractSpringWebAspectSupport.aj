package com.springsource.insight.plugin.springweb;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.collection.OperationCollector;

/**
 * Abstract Base Class for the Spring Web Plugin
 */
public abstract aspect AbstractSpringWebAspectSupport extends AbstractOperationCollectionAspect {
    public AbstractSpringWebAspectSupport() {
        super();
    }

    public AbstractSpringWebAspectSupport(OperationCollector collector) {
        super(collector);
    }

    @Override
    public String getPluginName() {
        return SpringWebPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
