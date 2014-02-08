/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.insight.plugin.springweb;

import java.util.Collection;
import java.util.List;

import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.metrics.MetricsGenerator;
import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceAnalyzer;
import com.springsource.insight.intercept.trace.TraceErrorAnalyzer;
import com.springsource.insight.plugin.springweb.controller.ControllerEndPointAnalyzer;
import com.springsource.insight.plugin.springweb.http.ClientHttpRequestExternalResourceAnalyzer;
import com.springsource.insight.plugin.springweb.http.ClientHttpRequestTraceErrorAnalyzer;
import com.springsource.insight.plugin.springweb.remoting.HttpInvokerRequestExecutorExternalResourceAnalyzer;
import com.springsource.insight.plugin.springweb.remoting.HttpInvokerRequestExecutorTraceErrorAnalyzer;
import com.springsource.insight.plugin.springweb.validation.ValidationErrorsMetricsGenerator;
import com.springsource.insight.util.ArrayUtil;

public class SpringWebPluginRuntimeDescriptor extends PluginRuntimeDescriptor {
    public static final String PLUGIN_NAME = "spring-web";
    private static final SpringWebPluginRuntimeDescriptor	INSTANCE=new SpringWebPluginRuntimeDescriptor();
    private static final List<? extends EndPointAnalyzer>	epAnalyzers=
    		ArrayUtil.asUnmodifiableList(ControllerEndPointAnalyzer.getInstance());
    private static final List<? extends TraceErrorAnalyzer>	errAnalyzers=
    		ArrayUtil.asUnmodifiableList(ClientHttpRequestTraceErrorAnalyzer.getInstance(),
    				HttpInvokerRequestExecutorTraceErrorAnalyzer.getInstance());
    private static final List<? extends ExternalResourceAnalyzer>	extResAnalyzers=
       		ArrayUtil.asUnmodifiableList(ClientHttpRequestExternalResourceAnalyzer.getInstance(),
       				HttpInvokerRequestExecutorExternalResourceAnalyzer.getInstance());
    private static final List<? extends MetricsGenerator>	mGenerators=
	    	ArrayUtil.asUnmodifiableList(ValidationErrorsMetricsGenerator.getInstance());
    
    private SpringWebPluginRuntimeDescriptor () {
        super();
    }

    public static final SpringWebPluginRuntimeDescriptor getInstance() {
    	return INSTANCE;
    }
    
    @Override
    public Collection<? extends ExternalResourceAnalyzer> getExternalResourceAnalyzers() {
    	return extResAnalyzers;
    }
    
    @Override
    public Collection<? extends EndPointAnalyzer> getEndPointAnalyzers() {
        return epAnalyzers;
    }
    
    @Override
    public Collection<? extends TraceErrorAnalyzer> getTraceErrorAnalyzers() {
        return errAnalyzers;
    }
    
    @Override
    public Collection<? extends MetricsGenerator> getMetricsGenerators() {
    	return mGenerators;
    }
    
    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }
}
