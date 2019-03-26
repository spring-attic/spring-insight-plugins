/**
 * Copyright (c) 2010-2012 Axon Framework All Rights Reserved.
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
package org.axonframework.insight.plugin.axon;

import java.util.Collection;
import java.util.List;

import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;
import com.springsource.insight.util.ArrayUtil;

public class AxonPluginRuntimeDescriptor extends PluginRuntimeDescriptor {
	public static final String PLUGIN_NAME = "axon";
	private static final AxonPluginRuntimeDescriptor	INSTANCE=new AxonPluginRuntimeDescriptor();
	private static final List<? extends EndPointAnalyzer>	epAnalyzers=ArrayUtil.asUnmodifiableList(
			CommandHandlerEndPointAnalyzer.getInstance(), SagaOperationEndPointAnalyzer.getInstance(),
			EventHandlerEndPointAnalyzer.getInstance());

	private AxonPluginRuntimeDescriptor () {
		super();
	}

	public static final AxonPluginRuntimeDescriptor getInstance() {
		return INSTANCE;
	}

	@Override
    public Collection<? extends EndPointAnalyzer> getEndPointAnalyzers() {
        return epAnalyzers;
    }
    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }
    
    @Override
    public String getPublisher() {
        return "Joris Kuipers (joris.kuipers@trifork.nl), Allard Buijze (allard.buijze@trifork.nl)";
    }
    
    @Override
    public String getHref(){
    	return "https://axoniq.io";
    }

}
