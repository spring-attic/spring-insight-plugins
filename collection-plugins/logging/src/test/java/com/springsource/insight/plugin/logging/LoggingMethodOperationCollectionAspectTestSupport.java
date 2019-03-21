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
package com.springsource.insight.plugin.logging;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.StringFormatterUtils;

/**
 *
 */
public abstract class LoggingMethodOperationCollectionAspectTestSupport
        extends OperationCollectionAspectTestSupport {

    protected LoggingMethodOperationCollectionAspectTestSupport() {
        super();
    }

    protected Operation assertLoggingOperation(Class<?> logger, String level, String msg, Throwable t) {
        Operation op = getLastEntered();
        assertNotNull("No operation extracted", op);
        assertEquals("Mismatched type", LoggingDefinitions.TYPE, op.getType());
        assertEquals("Mismatched framework", logger.getName(), op.get(LoggingDefinitions.FRAMEWORK_ATTR, String.class));
        assertEquals("Mismatched level", level, op.get(LoggingDefinitions.LEVEL_ATTR, String.class));
        assertEquals("Mismatched message", msg, op.get(LoggingDefinitions.MESSAGE_ATTR, String.class));

        if (t != null) {
            assertEquals("Mismatched exception",
                    StringFormatterUtils.formatStackTrace(t),
                    op.get(LoggingDefinitions.EXCEPTION_ATTR, String.class));
        }

        return op;
    }
}
