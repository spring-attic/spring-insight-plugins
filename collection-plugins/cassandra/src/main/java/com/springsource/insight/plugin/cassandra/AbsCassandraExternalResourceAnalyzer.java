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
package com.springsource.insight.plugin.cassandra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.topology.AbstractExternalResourceAnalyzer;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.ListUtil;

public abstract class AbsCassandraExternalResourceAnalyzer extends AbstractExternalResourceAnalyzer {
	public AbsCassandraExternalResourceAnalyzer(OperationType type) {
	    super(type);
	}

	public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace, Collection<Frame> frames) {
		if (ListUtil.size(frames) <= 0) {
		    return Collections.emptyList();
		}

		List<ExternalResourceDescriptor> queueDescriptors = new ArrayList<ExternalResourceDescriptor>(frames.size());
		for (Frame cacheFrame : frames) {
			Operation op = cacheFrame.getOperation();
			String server = op.get("server", String.class, "");
			
			int port=0;
			String host=server;
			int indx=server.lastIndexOf(":");
			if (indx>0) {
				host=server.substring(0,indx);
				try {
					port=Integer.parseInt(server.substring(indx+1));
				}
				catch(Exception e) {
					// invalid port
				}
			}

			String hashString = MD5NameGenerator.getName(server);
            String color = colorManager.getColor(op);
            
			ExternalResourceDescriptor descriptor =
			        new ExternalResourceDescriptor(cacheFrame, "server:" + hashString, server,
                       			                   ExternalResourceType.DATABASE.name(), "Cassandra",
                       			                   host, port, color, false);
			queueDescriptors.add(descriptor);            
		}

		return queueDescriptors;
	}
}