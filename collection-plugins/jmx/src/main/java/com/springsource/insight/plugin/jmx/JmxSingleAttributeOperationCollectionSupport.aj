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

package com.springsource.insight.plugin.jmx;

import java.io.IOException;

import javax.management.ObjectName;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.StringFormatterUtils;
import com.springsource.insight.util.StringUtil;

/**
 * Serves as base class for aspects that instrument a single attribute get/set
 */
public abstract aspect JmxSingleAttributeOperationCollectionSupport extends JmxAttributeOperationCollectionSupport {
    protected JmxSingleAttributeOperationCollectionSupport(String actionName) {
        super(actionName);
    }

    protected JmxSingleAttributeOperationCollectionSupport(String actionName, OperationCollector collector) {
        super(actionName, collector);
    }

    protected Operation createAttributeOperation(JoinPoint jp, ObjectName name, String attrName) {
        return createAttributeOperation(jp, name)
                .label(createLabel(action, attrName))
                .putAnyNonEmpty(JmxPluginRuntimeDescriptor.ATTR_NAME_PROP, attrName)
                ;
    }

    static final String createLabel(String actionName, String attrName) {
        try {
            return StringFormatterUtils.appendCapitalized(
                    new StringBuilder(StringUtil.getSafeLength(actionName) + StringUtil.getSafeLength(attrName) + 1),
                    actionName)
                    .append(' ').append(attrName)
                    .toString()
                    ;
        } catch (IOException e) {
            throw new RuntimeException("createLabel(" + actionName + ")[" + attrName + "]"
                    + " unexpected " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }
}
