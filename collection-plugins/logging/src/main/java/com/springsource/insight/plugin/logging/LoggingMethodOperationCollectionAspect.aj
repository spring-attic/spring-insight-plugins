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
package com.springsource.insight.plugin.logging;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.StringFormatterUtils;

/**
 * 
 */
public abstract aspect LoggingMethodOperationCollectionAspect extends MethodOperationCollectionAspect {

    protected Operation createOperation(JoinPoint jp, Class<?> logger, String level, String msg, Throwable t) {
        Operation   op=super.createOperation(jp)
                        .type(LoggingDefinitions.TYPE)
                        .label(level + ": " + truncateMessage(msg))
                        .put(LoggingDefinitions.FRAMEWORK_ATTR, logger.getName())
                        .put(LoggingDefinitions.LEVEL_ATTR, level)
                        .putAnyNonEmpty(LoggingDefinitions.MESSAGE_ATTR, msg)
                        ;
        if (t != null) {
            op.put(LoggingDefinitions.EXCEPTION_ATTR, StringFormatterUtils.formatStackTrace(t));
        }
        return op;
    }

    static final int    MAX_LABEL_MESSAGE_LENGTH=80;
    static String truncateMessage (String msg) {
        if ((msg == null) || (msg.length() < MAX_LABEL_MESSAGE_LENGTH)) {
            return msg;
        }

        return msg.substring(0, MAX_LABEL_MESSAGE_LENGTH) + " ...";
    }
    
    @Override
    public String getPluginName() { return LoggingPluginRuntimeDescriptor.PLUGIN_NAME; }
}
