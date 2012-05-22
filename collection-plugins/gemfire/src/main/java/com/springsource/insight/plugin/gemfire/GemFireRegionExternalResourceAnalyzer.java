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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.springsource.insight.intercept.color.ColorManager;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.topology.ExternalResourceAnalyzer;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;

public class GemFireRegionExternalResourceAnalyzer implements ExternalResourceAnalyzer {

	public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace) {
		Collection<Frame> frames = trace.getLastFramesOfType(GemFireDefenitions.TYPE_REGION.getType());		
		if ((frames == null) || frames.isEmpty()) {
		    return Collections.emptyList();
		}

		List<ExternalResourceDescriptor> descriptors = new LinkedList<ExternalResourceDescriptor>();
		for (Frame frame : frames) {			
            Operation       op = frame.getOperation();
			Object          regionFullPathObj = op.get(GemFireDefenitions.FIELD_PATH);
			String          regionFullPath = (regionFullPathObj == null) ? null : regionFullPathObj.toString();
			OperationList   servers = op.get(GemFireDefenitions.FIELD_SERVERS, OperationList.class);
            String color = ColorManager.getInstance().getColor(op);
			if ((servers == null) || (servers.size() <= 0)) {
				continue;
			}
			
			for (int i = 0; i < servers.size(); i++) {
				String server = servers.get(i).toString();
				String name = MD5NameGenerator.getName(server+regionFullPath);
				ExternalResourceDescriptor desc = new ExternalResourceDescriptor(frame, GemFireDefenitions.GEMFIRE + ":" + name, regionFullPath, ExternalResourceType.KVSTORE.name(), GemFireDefenitions.GEMFIRE, server, -1, color);
				descriptors.add(desc);
			}			
		}
		
		return descriptors;
	}
}
