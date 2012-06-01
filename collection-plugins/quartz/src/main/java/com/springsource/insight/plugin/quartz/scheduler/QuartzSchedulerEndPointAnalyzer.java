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
package com.springsource.insight.plugin.quartz.scheduler;

import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;

/**
 * 
 */
public class QuartzSchedulerEndPointAnalyzer implements EndPointAnalyzer {
    public QuartzSchedulerEndPointAnalyzer() {
        super();
    }

    public EndPointAnalysis locateEndPoint(Trace trace) {
        Frame     frame=trace.getFirstFrameOfType(QuartzSchedulerDefinitions.TYPE);
        Operation op=(frame == null) ? null : frame.getOperation();
        if (op == null) {
            return null;
        }

        return new EndPointAnalysis(EndPointName.valueOf(op), op.getLabel(), op.getLabel(), 0, op);
    }

}
