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

package com.springsource.insight.plugin.jaxrs;

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

public class JaxrsExternalResourceAnalyzer extends AbstractExternalResourceAnalyzer {
    private static final JaxrsExternalResourceAnalyzer INSTANCE = new JaxrsExternalResourceAnalyzer();

    private JaxrsExternalResourceAnalyzer() {
        super(JaxrsDefinitions.TYPE);
    }

    public static final JaxrsExternalResourceAnalyzer getInstance() {
        return INSTANCE;
    }

    public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace, Collection<Frame> frames) {
        if (ListUtil.size(frames) <= 0) {
            return Collections.emptyList();
        }

        List<ExternalResourceDescriptor> descriptors = new ArrayList<ExternalResourceDescriptor>(frames.size());
        for (Frame frame : frames) {
            Operation op = frame.getOperation();

            String path = op.get(JaxrsDefinitions.REQ_TEMPLATE, String.class);
            if (StringUtil.isEmpty(path)) {
                continue;
            }

            String hashString = MD5NameGenerator.getName(path);
            String color = colorManager.getColor(op);
            ExternalResourceDescriptor desc = new ExternalResourceDescriptor(frame,
                    JaxrsDefinitions.TYPE.getName() + ":" + hashString,
                    path,
                    ExternalResourceType.WEB_SERVICE.name(),
                    JaxrsDefinitions.TYPE.getName(),
                    color,
                    true);
            descriptors.add(desc);
        }

        return descriptors;
    }
}
