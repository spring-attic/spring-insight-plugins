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

package com.springsource.insight.plugin.jmx;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.plugin.PluginRuntimeDescriptor;

/**
 * 
 */
public class JmxPluginRuntimeDescriptor extends PluginRuntimeDescriptor {
    public static final String PLUGIN_NAME = "javax-management";
    public static final String	BEAN_NAME_PROP="beanName";
	public static final OperationType	ATTR=OperationType.valueOf("javax-mgmt-attr");
		public static final String	ACTION_PROP="action", ATTR_NAME_PROP="attrName", ATTR_VALUE_PROP="attrVal", ATTR_LIST_PROP="attrList";
			public static final String	GET_ACTION="get", SET_ACTION="set";
	public static final OperationType	INVOKE=OperationType.valueOf("javax-mgmt-invoke");

	private final List<EndPointAnalyzer> epAnalyzers;

	private static class LazyFieldHolder {
		@SuppressWarnings("synthetic-access")
		private static final JmxPluginRuntimeDescriptor	INSTANCE=new JmxPluginRuntimeDescriptor();
	}

	private JmxPluginRuntimeDescriptor () {
		epAnalyzers = Collections.unmodifiableList(Arrays.asList((EndPointAnalyzer) JmxInvocationEndPointAnalyzer.getInstance()));
	}

	@SuppressWarnings("synthetic-access")
	public static final JmxPluginRuntimeDescriptor getInstance() {
		return LazyFieldHolder.INSTANCE;
	}

	@Override
	public Collection<? extends EndPointAnalyzer> getEndPointAnalyzers() {
		return epAnalyzers;
	}

	@Override
	public String getPluginName() {
		return PLUGIN_NAME;
	}
}
