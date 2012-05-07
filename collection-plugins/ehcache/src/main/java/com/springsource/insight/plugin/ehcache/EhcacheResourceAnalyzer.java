/**
 * Copyright 2009-2011 the original author or authors.
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

package com.springsource.insight.plugin.ehcache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.topology.ExternalResourceAnalyzer;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;

public class EhcacheResourceAnalyzer implements ExternalResourceAnalyzer {
	public EhcacheResourceAnalyzer () {
	    super();
	}

	public List<ExternalResourceDescriptor> locateExternalResourceName(Trace trace) {
		Collection<Frame> cacheFrames = trace.getLastFramesOfType(EhcacheDefinitions.CACHE_OPERATION);
		if ((cacheFrames == null) || cacheFrames.isEmpty()) {
		    return Collections.emptyList();
		}

		List<ExternalResourceDescriptor> queueDescriptors =
		        new ArrayList<ExternalResourceDescriptor>(cacheFrames.size());
		for (Frame cacheFrame : cacheFrames) {
			Operation op = cacheFrame.getOperation();

			String label = buildLabel(op);

			String hashString = MD5NameGenerator.getName(label);

			ExternalResourceDescriptor descriptor =
			        new ExternalResourceDescriptor(cacheFrame,
                       			                   EhcacheDefinitions.VENDOR_NAME + ":" + hashString,
                       			                   label,
                       			                   ExternalResourceType.CACHE.name(),
                       			                   EhcacheDefinitions.VENDOR_NAME);
			queueDescriptors.add(descriptor);            
		}

		return queueDescriptors;
	}

	private String buildLabel(Operation op) {
		return op.get(EhcacheDefinitions.NAME_ATTRIBUTE, String.class, "");
	}
}
