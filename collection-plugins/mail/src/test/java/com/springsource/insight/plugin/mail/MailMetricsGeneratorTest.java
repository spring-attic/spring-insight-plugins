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
package com.springsource.insight.plugin.mail;

import java.util.List;

import com.springsource.insight.intercept.metrics.AbstractMetricsGenerator;
import com.springsource.insight.intercept.metrics.AbstractMetricsGeneratorTest;
import com.springsource.insight.intercept.metrics.MetricsBag;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.util.IDataPoint;

public class MailMetricsGeneratorTest extends AbstractMetricsGeneratorTest {
    public MailMetricsGeneratorTest() {
        super(MailSendMetricsGenerator.getInstance());
    }

    @Override
    protected void validateMetricsBags(List<MetricsBag> mbs) {
        assertEquals(2, mbs.size());

        MetricsBag mb = mbs.get(0);

        List<String> keys = mb.getMetricKeys();
        assertEquals(3, keys.size());

        assertEquals("opExtKey", mb.getResourceKey().getName());

        List<IDataPoint> points = mb.getPoints(AbstractMetricsGenerator.EXECUTION_TIME);
        assertEquals(1, points.size());
        assertEquals(160.0, points.get(0).getValue(), .5);

        points = mb.getPoints(AbstractMetricsGenerator.INVOCATION_COUNT);
        assertEquals(1, points.size());
        assertEquals(1.0, points.get(0).getValue(), .0001);

        points = mb.getPoints(MailSendMetricsGenerator.MAIL_SIZE_METRIC);
        assertEquals(1, points.size());
        assertEquals(256.0, points.get(0).getValue(), 0.01);
    }

    @Override
    protected List<Frame> makeFrame() {
        List<Frame> frames = super.makeFrame();
        for (Frame frame : frames) {
            Operation op = frame.getOperation();
            op.put("size", 256);
        }

        return frames;
    }
}