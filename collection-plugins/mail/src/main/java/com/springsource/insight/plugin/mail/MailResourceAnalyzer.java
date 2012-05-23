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
package com.springsource.insight.plugin.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.color.ColorManager;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.topology.ExternalResourceAnalyzer;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;

public class MailResourceAnalyzer implements ExternalResourceAnalyzer {
	public static final String RESOURCE_TYPE=ExternalResourceType.EMAIL.name();
	public List<ExternalResourceDescriptor> locateExternalResourceName(Trace trace) {
		List<Frame> mailFrames = trace.getLastFramesOfType(MailDefinitions.SEND_OPERATION);
		if ((mailFrames == null) || mailFrames.isEmpty()) {
            return Collections.emptyList();
		}

		List<ExternalResourceDescriptor> mailDescriptors = new ArrayList<ExternalResourceDescriptor>(mailFrames.size());
		for (Frame mailFrame : mailFrames) {
			Operation op = mailFrame.getOperation();

			String host = op.get(MailDefinitions.SEND_HOST, String.class);            
			Integer portProperty = op.get(MailDefinitions.SEND_PORT, Integer.class);
			int port = portProperty == null ? -1 : portProperty.intValue();
			String protocol = op.get(MailDefinitions.SEND_PROTOCOL, String.class); 
			String label = protocol.toUpperCase() + ":" + host + ((port > 0) ? (":" + port) : "");
			String hashString = MD5NameGenerator.getName(label);
            String color = ColorManager.getInstance().getColor(op);

			ExternalResourceDescriptor descriptor = new ExternalResourceDescriptor(
					mailFrame, protocol + ":" + hashString, label, RESOURCE_TYPE, protocol, host, port, color);
			mailDescriptors.add(descriptor);            
		}

		return mailDescriptors;
	}

}
