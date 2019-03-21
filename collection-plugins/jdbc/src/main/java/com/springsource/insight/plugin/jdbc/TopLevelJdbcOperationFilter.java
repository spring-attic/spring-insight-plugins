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
package com.springsource.insight.plugin.jdbc;

import static com.springsource.insight.intercept.trace.FrameUtil.descendantFramesOfType;
import static com.springsource.insight.intercept.trace.FrameUtil.frameIDs;
import static com.springsource.insight.intercept.trace.FrameUtil.topLevelFramesOfType;

import java.util.HashSet;
import java.util.Set;

import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.operation.filter.OperationFilter;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameId;
import com.springsource.insight.intercept.trace.Trace;


public class TopLevelJdbcOperationFilter implements OperationFilter {
    private static final OperationType TYPE = JdbcOperationExternalResourceAnalyzer.TYPE;
    private static final TopLevelJdbcOperationFilter	INSTANCE=new TopLevelJdbcOperationFilter();

    private TopLevelJdbcOperationFilter () {
    	super();
    }

    public static final TopLevelJdbcOperationFilter getInstance() {
    	return INSTANCE;
    }

    public String getFilterLabel() {
        return "JDBC, Top-level";
    }

    public int matchingFrames(Trace trace) {
        return includeFrames(trace).size();
    }

    public Set<FrameId> includeFrames(Trace trace) {
        Set<Frame> frames = topLevelFramesOfType(trace.getRootFrame(), TYPE);
        return frameIDs(frames);
    }
    
    public Set<FrameId> excludeFrames(Trace trace) {
        Set<Frame> frames = new HashSet<Frame>();
        for (Frame topLevelFrame : topLevelFramesOfType(trace.getRootFrame(), TYPE)) {
            frames.addAll(descendantFramesOfType(topLevelFrame, TYPE));
        }
        return frameIDs(frames);
    }

    public boolean isInversable() {
        return false;
    }
    
}
