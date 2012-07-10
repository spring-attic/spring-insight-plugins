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
package com.springsource.insight.plugin.jms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.color.ColorManager;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.topology.ExternalResourceAnalyzer;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameUtil;
import com.springsource.insight.intercept.trace.Trace;

abstract class AbstractJMSResourceAnalyzer implements EndPointAnalyzer,ExternalResourceAnalyzer {
    
	static final String JMS = "JMS";
    protected final JMSPluginOperationType operationType;
    protected final boolean isIncoming;
    
    AbstractJMSResourceAnalyzer(JMSPluginOperationType type, boolean incoming) {
        this.operationType = type;
        this.isIncoming = incoming;
    }

    public EndPointAnalysis locateEndPoint(Trace trace) {
        Frame frame = trace.getFirstFrameOfType(operationType.getOperationType());
        if (frame == null) {
            return null;
        }

        return makeEndPoint(frame);
    }

    private EndPointAnalysis makeEndPoint(Frame frame) {
        Operation op = frame.getOperation();
        if (op != null) {
        	String label = buildLabel(op);
			String endPointLabel = JMS + "-" + label;
			
            String example = getExample(label);
            EndPointName endPointName = getName(label);
            
            return new EndPointAnalysis(endPointName, endPointLabel, example, 1, op);
        }
        
        return null;
    }
    
   public List<ExternalResourceDescriptor> locateExternalResourceName(Trace trace) {
		Collection<Frame> queueFrames = trace.getLastFramesOfType(operationType.getOperationType());
		if ((queueFrames == null) || queueFrames.isEmpty()) {
		    return Collections.emptyList();
		}

		List<ExternalResourceDescriptor> queueDescriptors=new ArrayList<ExternalResourceDescriptor>(queueFrames.size());
		for (Frame queueFrame : queueFrames) {
			Operation op = queueFrame.getOperation();

			String label = buildLabel(op);
			String host = op.get("host", String.class);            
			Integer portProperty = op.get("port", Integer.class);
			int port = portProperty == null ? -1 : portProperty.intValue();
            String color = ColorManager.getInstance().getColor(op);
			String hashString = MD5NameGenerator.getName(label + host + port + isIncoming);

			ExternalResourceDescriptor descriptor =
			        new ExternalResourceDescriptor(queueFrame,
			                                        JMS + ":" + hashString,
			                                        JMS + "-" + label,
			                                        ExternalResourceType.QUEUE.name(),
			                                        JMS,
			                                        host,
			                                        port,
                                                    color, isIncoming);
			queueDescriptors.add(descriptor);            
		}

		return queueDescriptors;
	}

    private EndPointName getName(String label) {
		return EndPointName.valueOf(label);
	}
    
    private String getExample(String label) {
    	return operationType.getEndPointPrefix() + label;
    }
    
    private String buildLabel(Operation op) {
    	String type = op.get("destinationType", String.class);
        String name = op.get("destinationName", String.class);

        return type + "#" + name;
	}
    
    public EndPointAnalysis locateEndPoint(Frame frame, int depth) {
        Frame parent = FrameUtil.getLastParentOfType(frame, operationType.getOperationType());
        
        if (parent != null) {
            return null;
        }
        
        return makeEndPoint(frame);
    }
    
    public int getScore(Frame frame, int depth) {
        return 1;
    }
    
    public OperationType[] getOperationTypes() {
        return new OperationType[] {operationType.getOperationType()};
    }
}
