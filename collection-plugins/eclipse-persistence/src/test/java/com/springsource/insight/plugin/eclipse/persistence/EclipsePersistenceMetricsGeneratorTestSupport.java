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

package com.springsource.insight.plugin.eclipse.persistence;

import org.junit.Assert;

import com.springsource.insight.intercept.metrics.AbstractMetricsGeneratorTest;
import com.springsource.insight.intercept.metrics.MetricsGenerator;
import com.springsource.insight.intercept.operation.OperationType;

/**
 * 
 */
public abstract class EclipsePersistenceMetricsGeneratorTestSupport
        extends AbstractMetricsGeneratorTest {
    protected final OperationType   opType;
    protected EclipsePersistenceMetricsGeneratorTestSupport(OperationType type) {
        Assert.assertNotNull("No operation type specified", type);
        this.opType = type;
    }

    @Override
    protected final MetricsGenerator getMetricsGenerator() {
        EclipsePersistenceMetricsGenerator  generator=createEclipsePersistenceMetricsGenerator();
        Assert.assertEquals("Mismatched generator operation", getOperationType(), generator.getOperationType());
        return generator;
    }

    @Override
    protected final OperationType getOperationType() {
        return opType;
    }

    protected abstract EclipsePersistenceMetricsGenerator createEclipsePersistenceMetricsGenerator ();
}
