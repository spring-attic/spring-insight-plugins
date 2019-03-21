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

package com.springsource.insight.plugin.springweb.validation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.springsource.insight.intercept.metrics.AbstractMetricsGenerator;
import com.springsource.insight.intercept.metrics.MetricsBag;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.resource.ResourceKey;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.ListUtil;

/**
 *
 */
public class ValidationErrorsMetricsGenerator extends AbstractMetricsGenerator {
    public static final OperationType TYPE = OperationType.valueOf("controller_validator");
    public static final String METRIC_KEY = ValidationJoinPointFinalizer.ERRORS_COUNT + ":type=counter";
    private static final ValidationErrorsMetricsGenerator INSTANCE = new ValidationErrorsMetricsGenerator();

    private ValidationErrorsMetricsGenerator() {
        super(TYPE);
    }

    public static final ValidationErrorsMetricsGenerator getInstance() {
        return INSTANCE;
    }

    @Override
    public List<MetricsBag> generateMetrics(Trace trace, ResourceKey endpointResourceKey, Collection<Frame> frames) {
        if (ListUtil.size(frames) <= 0) {
            return Collections.emptyList();
        }

        int totalErrors = 0;
        for (Frame frame : frames) {
            Operation op = frame.getOperation();
            Number errorsCount = op.get(ValidationJoinPointFinalizer.ERRORS_COUNT, Number.class);
            int numErrors = (errorsCount == null) ? 0 : errorsCount.intValue();
            if (numErrors <= 0) {
                continue;
            } else {
                totalErrors += numErrors;
            }
        }

        if (totalErrors <= 0) {
            return Collections.emptyList();
        }

        MetricsBag mb = MetricsBag.create(endpointResourceKey, trace.getRange());
        addCounterMetricToBag(trace, mb, METRIC_KEY, totalErrors);
        return Collections.singletonList(mb);
    }

    @Override
    public Collection<Frame> locateFrames(Trace trace) {
        return trace.getAllFramesOfType(getOperationType());
    }

}
