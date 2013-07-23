package com.springsource.insight.plugin.jsf;


import java.util.Collection;
import java.util.List;

import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;
import com.springsource.insight.util.ArrayUtil;

public class JsfPluginRuntimeDescriptor extends PluginRuntimeDescriptor {
	
    public static final String PLUGIN_NAME = "jsf";
    private static final JsfPluginRuntimeDescriptor	INSTANCE=new JsfPluginRuntimeDescriptor();
    private static final List<? extends EndPointAnalyzer>	epAnalyzers=
    		ArrayUtil.asUnmodifiableList(JSFActionEndPointAnalyzer.getInstance());

    private JsfPluginRuntimeDescriptor () {
    	super();
    }

    public static final JsfPluginRuntimeDescriptor getInstance() {
    	return INSTANCE;
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }
    @Override
    public Collection<? extends EndPointAnalyzer> getEndPointAnalyzers() {
        return epAnalyzers;
    }

}
