/**
 * Copyright 2009-2010 the original author or authors.
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

package com.springsource.insight.plugin.jta;

import com.springsource.insight.intercept.metrics.AbstractMetricsGeneratorTest;
import com.springsource.insight.intercept.operation.OperationType;

/**
 * 
 */
public abstract class JtaMetricsGeneratorTestSupport
        extends AbstractMetricsGeneratorTest {
    protected final OperationType   opType;
    protected JtaMetricsGeneratorTestSupport(final OperationType type) {
        if ((opType=type) == null) {
            throw new IllegalStateException("No operation type provided");
        }
    }

    @Override
    protected final JtaMetricsGenerator getMetricsGenerator() {
        return new JtaMetricsGenerator(opType);
    }

    @Override
    protected final OperationType getOperationType() {
        return opType;
    }

}
