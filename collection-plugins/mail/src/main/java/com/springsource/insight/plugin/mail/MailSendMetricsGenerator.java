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

import com.springsource.insight.intercept.metrics.AbstractExternalResourceMetricsGenerator;
import com.springsource.insight.intercept.metrics.MetricsBag;
import com.springsource.insight.intercept.metrics.MetricsBag.PointType;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.DataPoint;
import com.springsource.insight.util.time.TimeUtil;

public class MailSendMetricsGenerator extends AbstractExternalResourceMetricsGenerator {
    public static final String MAIL_SIZE_METRIC = "mailSize:type=bytes";
    private static final MailSendMetricsGenerator INSTANCE = new MailSendMetricsGenerator();

    private MailSendMetricsGenerator() {
        super(MailDefinitions.SEND_OPERATION);
    }

    public static final MailSendMetricsGenerator getInstance() {
        return INSTANCE;
    }

    @Override
    protected void addExtraFrameMetrics(Trace trace, Frame opTypeFrame, MetricsBag mb) {
        // Add the message size data point
        Operation op = opTypeFrame.getOperation();
        Number contentSize = op.get("size", Number.class);
        // OK if missing - the size is collected only if extra information is enabled
        if (contentSize == null) {
            return;
        }

        mb.add(MAIL_SIZE_METRIC, PointType.GAUGE);
        int time = TimeUtil.nanosToSeconds(trace.getRange().getStart());
        DataPoint responseSizePoint = new DataPoint(time, contentSize.doubleValue());
        mb.add(responseSizePoint, MAIL_SIZE_METRIC);
    }
}
