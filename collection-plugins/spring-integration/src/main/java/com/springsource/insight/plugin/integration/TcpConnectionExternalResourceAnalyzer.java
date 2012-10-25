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

package com.springsource.insight.plugin.integration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.topology.AbstractExternalResourceAnalyzer;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;

/**
 * Extracts {@link TcpConnectionOperationCollector#HOST_ADDRESS_ATTR} target addresses
 * as {@link ExternalResourceType#SERVER} {@link ExternalResourceDescriptor}-s
 */
public class TcpConnectionExternalResourceAnalyzer extends AbstractExternalResourceAnalyzer {
    public static final OperationType TYPE = OperationType.valueOf("integration_tcpconn");
    private static final TcpConnectionExternalResourceAnalyzer	INSTANCE=new TcpConnectionExternalResourceAnalyzer();

	private TcpConnectionExternalResourceAnalyzer() {
		super(TYPE);
	}

	public static final TcpConnectionExternalResourceAnalyzer getInstance() {
		return INSTANCE;
	}

	public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace, Collection<Frame> externalFrames) {
        if (ListUtil.size(externalFrames) <= 0) {
            return Collections.emptyList();
        }

        Set<ExternalResourceDescriptor> resSet=new HashSet<ExternalResourceDescriptor>(externalFrames.size());
        for (Frame frame : externalFrames) {
            ExternalResourceDescriptor  res=extractExternalResourceDescriptor(frame);
            if (res == null) {  // can happen if failed to parse the URI somehow
                continue;
            }

            if (!resSet.add(res)) {
                continue;   // debug breakpoint
            }
        }
        
        return resSet;
    }

    ExternalResourceDescriptor extractExternalResourceDescriptor (Frame frame) {
        Operation   op=(frame == null) ? null : frame.getOperation();
        String      addr=(op == null) ? null : op.get(TcpConnectionOperationCollector.HOST_ADDRESS_ATTR, String.class);
        if (StringUtil.isEmpty(addr)) {
            return null;
        }

        int     port=op.get(TcpConnectionOperationCollector.PORT_ATTR, Number.class).intValue();
        String	uri=op.get(OperationFields.URI, String.class);
        String	color=colorManager.getColor(op);
        return new ExternalResourceDescriptor(frame,
        									  MD5NameGenerator.getName(uri),
                                              op.getLabel() + " " + uri,
                                              ExternalResourceType.SERVER.name(),
                                              null,
                                              addr,
                                              port,
                                              color,
                                              false);
    }

}
