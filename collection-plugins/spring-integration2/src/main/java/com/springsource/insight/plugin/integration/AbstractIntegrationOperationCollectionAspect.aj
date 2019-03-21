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

package com.springsource.insight.plugin.integration;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;

import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.intercept.color.ColorManager.ColorParams;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.ExtraReflectionUtils;
import com.springsource.insight.util.ReflectionUtils;
import com.springsource.insight.util.logging.InsightLogManager;

/**
 *
 */
public abstract aspect AbstractIntegrationOperationCollectionAspect extends AbstractOperationCollectionAspect {
    private static final Field headersMapField;

    static {
        if ((headersMapField = ExtraReflectionUtils.getAccessibleField(MessageHeaders.class, "headers", Map.class)) == null) {
            InsightLogManager.getLogger(AbstractIntegrationOperationCollectionAspect.class.getName())
                    .warning("Cannot find message headers field")
            ;
        }
    }

    protected AbstractIntegrationOperationCollectionAspect() {
        super();
    }

    protected AbstractIntegrationOperationCollectionAspect(OperationCollector collector) {
        super(collector);
    }

    protected void colorForward(final Operation op, final Message<?> msg) {
        colorForward(op, (msg == null) ? null : msg.getHeaders());
    }

    protected void colorForward(final Operation op, final MessageHeaders hdrs) {
        colorForward(new ColorParams() {
            @SuppressWarnings("synthetic-access")
            public void setColor(String key, String value) {
                if ((hdrs == null) || (headersMapField == null)) {
                    return;
                }

                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) ReflectionUtils.getField(headersMapField, hdrs);
                    Object prev = map.put(key, value);
                    if (prev != null) {
                        if (_logger.isLoggable(Level.FINE)) {
                            _logger.fine("colorForward(" + key + ")[" + prev + "] => " + value);
                        }
                    }
                } catch (RuntimeException e) {
                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.fine("colorForward(" + key + ")[" + value + "]"
                                + " failed (" + e.getClass().getSimpleName() + ")"
                                + " to access headers field: " + e.getMessage());
                    }
                }
            }

            public Operation getOperation() {
                return op;
            }
        });
    }

    @Override
    public final String getPluginName() {
        return IntegrationPluginRuntimeDescriptor.PLUGIN_NAME;
    }

    @Override
    public boolean isMetricsGenerator() {
        return true; // This provides an external resource
    }
}
