/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.plugin.ehcache;

import java.util.Collection;
import java.util.Collections;

import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;
import com.springsource.insight.intercept.trace.collapse.CollapsingFrameHelperRegistrar;

public class EhcahePluginRuntimeDescriptor extends PluginRuntimeDescriptor {
	private static final EhcahePluginRuntimeDescriptor	INSTANCE=new EhcahePluginRuntimeDescriptor();
    private final Collection<CollapsingFrameHelperRegistrar> frameHelperRegistrars = Collections.singleton(CollapsingFrameHelperRegistrar.of(EhcacheDefinitions.CACHE_OPERATION)
                                                                                                                                         .with(EhcacheCollapsingFrameHelper.getInstance()));

	private EhcahePluginRuntimeDescriptor () {
		super();
	}

	public static final EhcahePluginRuntimeDescriptor getInstance() {
		return INSTANCE;
	}

    @Override
    public String getPluginName() {
        return EhcacheDefinitions.PLUGIN_NAME;
    }

    @Override
    public Collection<CollapsingFrameHelperRegistrar> getCollapsingFrameHelperRegistrars() {
        return frameHelperRegistrars;
    }
}
