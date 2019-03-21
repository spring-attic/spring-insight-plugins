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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
import org.mockito.Mockito;

import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.metrics.AbstractMetricsGenerator;
import com.springsource.insight.intercept.metrics.AbstractMetricsGeneratorTest;
import com.springsource.insight.intercept.metrics.MetricsBag;
import com.springsource.insight.intercept.metrics.MetricsBag.PointType;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.resource.ResourceKey;
import com.springsource.insight.intercept.server.ServerName;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;
import com.springsource.insight.util.DataPoint;
import com.springsource.insight.util.IDataPoint;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.time.TimeRange;

/**
 *
 */
public abstract class EclipsePersistenceMetricsGeneratorTestSupport extends AbstractMetricsGeneratorTest {
    protected EclipsePersistenceMetricsGeneratorTestSupport(EclipsePersistenceMetricsGenerator generator) {
        super(generator);
    }

    @Override
    public void testGenerateMetrics() {
        // we do NOT want metrics to be generated on the endpoint
    }

    @Test
    public void testExtraMetricsGeneration() {
        TimeRange range = new TimeRange(7365L, 3777347L);
        String actionName = "testExtraMetricsGeneration";
        Trace trace = createMockTrace(range, actionName);
        Frame root = trace.getRootFrame();
        EndPointName ep = EndPointName.valueOf(actionName);
        ResourceKey rKey = ep.makeKey();
        Collection<MetricsBag> mbList =
                ((EclipsePersistenceMetricsGenerator) gen).addExtraEndPointMetrics(trace, rKey, Collections.singletonList(root));
        assertEquals("Mismatched metrics count", 1, ListUtil.size(mbList));

        MetricsBag mb = ListUtil.getFirstMember(mbList);
        assertEquals("Mismatched resource key", rKey, mb.getResourceKey());
        assertEquals("Mismatched time range", trace.getRange(), mb.getTimeRange());

        String baseName = ((EclipsePersistenceMetricsGenerator) gen).getBaseMetricName(actionName);
        Collection<String> keys = mb.getMetricKeys();
        Map<String, PointType> suffixes = new TreeMap<String, MetricsBag.PointType>() {
            private static final long serialVersionUID = 1L;

            {
                put(AbstractMetricsGenerator.INVOCATION_COUNT, PointType.COUNTER);
                put(AbstractMetricsGenerator.EXECUTION_TIME, PointType.GAUGE);
            }
        };
        assertEquals("Mismatched number of keys - " + keys, suffixes.size(), keys.size());

        DataPoint expValue = DataPoint.dataPointFromRange(range);
        for (Map.Entry<String, PointType> se : suffixes.entrySet()) {
            String sfx = se.getKey();
            String keyName = baseName + "." + sfx;
            assertTrue("Missing " + keyName + " from " + keys, keys.contains(keyName));

            PointType expType = se.getValue(), actType = mb.getMetricType(keyName);
            assertEquals("Mismatched type for " + keyName, expType, actType);

            List<IDataPoint> dpList = mb.getPoints(keyName);
            assertEquals("Mismatched points size for " + keyName + " - " + dpList, 1, ListUtil.size(dpList));

            IDataPoint dp = dpList.get(0);
            assertEquals("Mismatched timestamp for " + keyName, expValue.getTime(), dp.getTime());

            switch (actType) {
                case COUNTER:
                    assertEquals("Mismatched value for " + keyName, 1.0d, dp.getValue(), 0.000001d);
                    break;

                case GAUGE:
                    assertEquals("Mismatched value for " + keyName, expValue.getValue(), dp.getValue(), 0.000001d);
                    break;

                default:
                    fail("Unknown value type for " + keyName + ": " + actType);
            }
        }
    }

    protected final Trace createMockTrace(TimeRange range, String actionName) {
        Frame frame = createMockFrame(range, actionName);
        return new Trace(ServerName.valueOf(actionName),
                ApplicationName.valueOf("localhost", actionName),
                new Date(),
                TraceId.valueOf(actionName),
                frame);
    }

    protected final Frame createMockFrame(TimeRange range, String actionName) {
        Operation op = new Operation()
                .type(gen.getOperationType())
                .label(actionName + ": " + range)
                .put(EclipsePersistenceDefinitions.ACTION_ATTR, actionName);
        Frame frame = createMockOperationWrapperFrame(op);
        Mockito.when(frame.getRange()).thenReturn(range);
        return frame;
    }
}
