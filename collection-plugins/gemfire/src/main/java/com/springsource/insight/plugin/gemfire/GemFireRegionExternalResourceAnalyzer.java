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

package com.springsource.insight.plugin.gemfire;

import java.util.ArrayList;
import java.util.List;

import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.topology.ExternalResourceAnalyzer;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;

public class GemFireRegionExternalResourceAnalyzer implements ExternalResourceAnalyzer {

	public List<ExternalResourceDescriptor> locateExternalResourceName(Trace trace) {
		List<Frame> frames = trace.getLastFramesOfType(GemFireDefenitions.TYPE_REGION.getType());		
		List<ExternalResourceDescriptor> descriptors = new ArrayList<ExternalResourceDescriptor>();
		
		for (Frame frame : frames) {			
			Object regionFullPathObj = frame.getOperation().get(GemFireDefenitions.FIELD_PATH);
			String regionFullPath = regionFullPathObj == null ? null : regionFullPathObj.toString();
			OperationList servers = (OperationList) frame.getOperation().get(GemFireDefenitions.FIELD_SERVERS);
			if (servers == null || servers.size() == 0) {
				continue;
			}
			
			for (int i = 0; i < servers.size(); i++) {
				String server = servers.get(i).toString();
				String name = MD5NameGenerator.getName(server);
				ExternalResourceDescriptor desc = new ExternalResourceDescriptor(frame, GemFireDefenitions.GEMFIRE + ":" + name, regionFullPath, ExternalResourceType.KVSTORE.name(), GemFireDefenitions.GEMFIRE, server, -1);
				descriptors.add(desc);
			}			
		}
		
		return descriptors;
	}
	

}
