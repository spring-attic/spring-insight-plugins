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

package com.springsource.insight.plugin.gemfire;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.topology.AbstractExternalResourceAnalyzer;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;

public class GemFireRemoteExternalResourceAnalyzer extends AbstractExternalResourceAnalyzer {
	private static final int EMPTY_PORT = -1;

    public GemFireRemoteExternalResourceAnalyzer () {
		super(GemFireDefenitions.TYPE_REMOTE.getType());
	}

	public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace, Collection<Frame> frames) {
		if (ListUtil.size(frames) <= 0) {
		    return Collections.emptyList();
		}

		List<ExternalResourceDescriptor> descriptors = new ArrayList<ExternalResourceDescriptor>(frames.size());
		for (Frame frame : frames) {			
            Operation       op = frame.getOperation();
            
            String hostname = op.get(GemFireDefenitions.FIELD_HOST, String.class);
            Number portObj = op.get(GemFireDefenitions.FIELD_PORT, Number.class);
            int port = portObj != null ? portObj.intValue() : EMPTY_PORT;
            
			if (StringUtil.isEmpty(hostname) || GemFireDefenitions.FIELD_UNKNOWN.equals(hostname)) {
				continue;
			}

            String color = colorManager.getColor(op);
			String label = createLabel(hostname, port);
			String name = createName(hostname, port, label);
			
			ExternalResourceDescriptor desc =
			        new ExternalResourceDescriptor(frame,
			                                       name,
			                                       label,
			                                       ExternalResourceType.KVSTORE.name(),
			                                       GemFireDefenitions.GEMFIRE,
			                                       hostname,
			                                       port,
			                                       color,
			                                       false);
			descriptors.add(desc);
		}
		
		return descriptors;
	}
	
	public final static String createLabel(String hostname, int port) {
	    StringBuilder builder = new StringBuilder((hostname != null ? hostname.length() : 4) + 6);
	    
	    builder.append(hostname);
	    builder.append(':');
	    builder.append(port);
	    
	    return builder.toString();
	}
	
	public final static String createName(String hostname, int port) {
	    return createName(hostname, port, null);
	}
	
	public final static String createName(String hostname, int port, String label) {
	    String labelToUse = label != null ? label : createLabel(hostname, port);
	    StringBuilder builder = new StringBuilder(GemFireDefenitions.GEMFIRE.length() + 1 + labelToUse.length());
	    
	    builder.append(GemFireDefenitions.GEMFIRE);
	    builder.append(':');
	    builder.append(labelToUse);
	    
	    String name = MD5NameGenerator.getName(builder.toString());
	    
	    builder.delete(0, builder.length());
	    builder.ensureCapacity(GemFireDefenitions.GEMFIRE.length() + 1 + name.length());
	    
	    builder.append(GemFireDefenitions.GEMFIRE);
	    builder.append(':');
	    builder.append(name);
	    
	    return builder.toString();
	}
}
