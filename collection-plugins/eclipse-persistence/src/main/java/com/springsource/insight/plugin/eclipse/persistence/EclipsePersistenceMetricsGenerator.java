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

package com.springsource.insight.plugin.eclipse.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.springsource.insight.intercept.metrics.AbstractMetricsGenerator;
import com.springsource.insight.intercept.metrics.MetricsBag;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.resource.ResourceKey;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;
import com.springsource.insight.util.time.TimeRange;

/**
 * 
 */
public abstract class EclipsePersistenceMetricsGenerator extends AbstractMetricsGenerator {
    protected EclipsePersistenceMetricsGenerator(OperationType type) {
        super(type);
    }

    @Override
    protected Collection<MetricsBag> addExtraEndPointMetrics(Trace trace, ResourceKey endpointResourceKey, Collection<Frame> frames) {
    	if (ListUtil.size(frames) <= 0) {
    		return Collections.emptyList();
    	}

    	Collection<MetricsBag>	mbList=null;
    	TimeRange				traceRange=trace.getRange();
    	for (Frame frame : frames) {
    		Operation	op=frame.getOperation();
    		String		actionName=op.get(EclipsePersistenceDefinitions.ACTION_ATTR, String.class);
    		if (StringUtil.isEmpty(actionName)) {
    			continue;
    		}
    		
    		String	baseMetricName=getBaseMetricName(actionName);
    		if (StringUtil.isEmpty(baseMetricName)) {
    			continue;
    		}
    		
    		MetricsBag mb = MetricsBag.create(endpointResourceKey, traceRange);
    		addCounterMetricToBag(frame, mb, baseMetricName + "." + INVOCATION_COUNT, 1);
    		addGaugeMetricToBag(frame, mb, baseMetricName + "." + EXECUTION_TIME);
    		
    		if (mbList == null) {
    			mbList = new ArrayList<MetricsBag>(frames.size());
    		}
    		mbList.add(mb);
    	}

    	if (mbList == null) {
    		return Collections.emptyList();
    	} else {
    		return  mbList;
    	}
    }
    
    //because this class inherits from  AbstractMetricsGenerator and we don't want to generate the default metrics on the endpoint (only the extras)
    //this should be changed sometime (the AbstractMetricsGenerator shouldn't be directly inheritable)
    @Override
    protected Collection<MetricsBag> generateFramesMetrics (Trace trace, ResourceKey endpointResourceKey, Collection<Frame> frames) {
    	return new ArrayList<MetricsBag>();
	}


    protected String getBaseMetricName (String actionName) {
		if (StringUtil.isEmpty(actionName)) {
			return null;
		}

    	return new StringBuilder(EclipsePersistenceDefinitions.ACTION_ATTR.length() + actionName.length() + 1)
    					.append(EclipsePersistenceDefinitions.ACTION_ATTR)
    					.append('.')
    					.append(actionName)
    					.toString()
    					;
    }
}
