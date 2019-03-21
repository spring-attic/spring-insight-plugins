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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.springsource.insight.intercept.metrics.AbstractExternalResourceMetricsGenerator;
import com.springsource.insight.intercept.metrics.AbstractMetricsGeneratorTest;
import com.springsource.insight.intercept.metrics.MetricsBag;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.MapUtil;

/**
 * Checks a serialized version of the trace used in the integration tests
 */
public class JdbcTraceIntegrationTest extends AbstractMetricsGeneratorTest {
    private static final List<OperationType> METRICS_OPS =
            Arrays.asList(JdbcDriverExternalResourceAnalyzer.TYPE, JdbcOperationExternalResourceAnalyzer.TYPE);
    private static final Map<OperationType, AbstractExternalResourceMetricsGenerator> metricsGenerators =
            Collections.unmodifiableMap(
                    toGeneratorsMap(createExternalResourceMetricsGenerators(false, METRICS_OPS)));

    public JdbcTraceIntegrationTest() {
        super(new AbstractExternalResourceMetricsGenerator(OperationType.UNKNOWN) { /* nothing extra */
        });
    }

    @Test
    public void testJdbcTrace() throws Exception {
        Trace trace = loadTrace("JdbcTraceTest");
        Map<OperationType, List<MetricsBag>> result = analyzeTrace("testJdbcTrace", trace, metricsGenerators.values());
        assertEquals("Mismatched results size", 1, MapUtil.size(result));

        List<MetricsBag> jdbcOperationMetrics = result.get(JdbcOperationExternalResourceAnalyzer.TYPE);
        assertEquals("Mismatched operation metrics size: " + jdbcOperationMetrics, 2, ListUtil.size(jdbcOperationMetrics));
        for (int index = 0; index < jdbcOperationMetrics.size(); index++) {
            assertInvocationCountValue("testJdbcTrace[" + index + "]", jdbcOperationMetrics.get(index), 1L);
        }
    }
}
