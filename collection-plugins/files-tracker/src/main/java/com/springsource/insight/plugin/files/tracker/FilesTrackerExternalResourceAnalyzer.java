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

package com.springsource.insight.plugin.files.tracker;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.springsource.insight.intercept.color.ColorManager;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.topology.ExternalResourceAnalyzer;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;

public class FilesTrackerExternalResourceAnalyzer implements ExternalResourceAnalyzer {
    private final ColorManager  colorManager;
    public FilesTrackerExternalResourceAnalyzer () {
        colorManager = ColorManager.getInstance();
    }

	public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace) {
		Collection<Frame> frames = trace.getLastFramesOfType(FilesTrackerDefinitions.TYPE);		
		if (ListUtil.size(frames) <= 0) {
		    return Collections.emptyList();
		}

		List<ExternalResourceDescriptor> descriptors = new LinkedList<ExternalResourceDescriptor>();
		for (Frame frame : frames) {			
            Operation    op=frame.getOperation();
			String       path=op.get(FilesTrackerDefinitions.PATH_ATTR, String.class);
			if (StringUtil.isEmpty(path)) {
				continue;
			}

			String hashString = MD5NameGenerator.getName(path);
			String color = colorManager.getColor(op);
			ExternalResourceDescriptor desc =
			        new ExternalResourceDescriptor(frame,
			                                       FilesTrackerDefinitions.TYPE.getName() + ":" + hashString,
			                                       path,
			                                       ExternalResourceType.FILESTORE.name(),
			                                       FilesTrackerDefinitions.TYPE.getName(),
			                                       color, false);
			descriptors.add(desc);			
		}
		
		return descriptors;
	}
}
