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
package com.springsource.insight.plugin.jms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.color.ColorManager;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.metrics.AbstractMetricsGenerator;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.topology.ExternalResourceAnalyzer;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;

public abstract class AbstractJMSResourceAnalyzer implements ExternalResourceAnalyzer {
    public static final String JMS = "JMS";
    /**
     * The <U>static</U> score value assigned to endpoints - <B>Note:</B>
     * we return a score of {@link EndPointAnalysis#CEILING_LAYER_SCORE} so as
     * to let other endpoints &quot;beat&quot; this one
     */
    public static final int	DEFAULT_SCORE = EndPointAnalysis.CEILING_LAYER_SCORE;

    protected final JMSPluginOperationType operationType;
    protected final boolean isIncoming;

    protected AbstractJMSResourceAnalyzer(JMSPluginOperationType type, boolean incoming) {
        this.operationType = type;
        this.isIncoming = incoming;
    }
    
    public OperationType getOperationType(){
		return operationType.getOperationType();
	}

    public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace) {
        return locateExternalResourceName(trace,  locateFrames(trace));
    }

    public Collection<Frame> locateFrames(Trace trace) {
        return AbstractMetricsGenerator.locateDefaultMetricsFrames(trace, getOperationType());
    }

    public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace, Collection<Frame> queueFrames) {
        if (ListUtil.size(queueFrames) <= 0) {
            return Collections.emptyList();
        }

        List<ExternalResourceDescriptor> queueDescriptors=new ArrayList<ExternalResourceDescriptor>(queueFrames.size());
        ColorManager					 colorManager=ColorManager.getInstance();
        for (Frame queueFrame : queueFrames) {
            ExternalResourceDescriptor descriptor = createExternalResourceDescriptor(colorManager, queueFrame);
            queueDescriptors.add(descriptor);
        }

        return queueDescriptors;
    }

    ExternalResourceDescriptor createExternalResourceDescriptor (ColorManager colorManager, Frame queueFrame) {
        Operation op = queueFrame.getOperation();
        String label = buildLabel(op);

        OperationMap connectionData = op.get(JMSPluginUtils.CONNECTION_DATA, OperationMap.class);

        String host = connectionData == null ? null : connectionData.get("host", String.class);
        Number portProperty = connectionData == null ? null : connectionData.get("port", Number.class);
        int port = portProperty == null ? -1 : portProperty.intValue();

        String color = colorManager.getColor(op);
        String hashString = buildNameHash(label, host, port);

        return new ExternalResourceDescriptor(queueFrame,
                JMS + ":" + hashString,
                JMS + "-" + label,
                ExternalResourceType.QUEUE.name(),
                JMS,
                host,
                port,
                color, isIncoming);
    }

    public static String buildLabel(Operation op) {
        String type = op.get("destinationType", String.class);
        String name = op.get("destinationName", String.class);

        return buildLabel(type, name);
    }

    public static String buildLabel(String destType, String destName) {
        DestinationType type = DestinationType.fromLabel(destType);

        if (type.isTemporary()) {
            return destType;
        }

        StringBuilder sb = new StringBuilder(StringUtil.getSafeLength(destType) + 1 + StringUtil.getSafeLength(destName));

        sb.append(destType)
        .append('#')
        .append(destName);

        return sb.toString();
    }

    public static String buildNameHash(String label, String host, int port) {
        StringBuilder sb = new StringBuilder(StringUtil.getSafeLength(label) + 5 /* max. port string length */ + StringUtil.getSafeLength(host));

        sb.append(label)
        .append(host)
        .append(port);

        return MD5NameGenerator.getName(sb.toString());
    }
}
