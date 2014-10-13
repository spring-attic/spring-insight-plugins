/**
 * Copyright (c) 2010-2012 Axon Framework All Rights Reserved.
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
package org.axonframework.insight.plugin.axon;

import org.aspectj.lang.JoinPoint;
import org.axonframework.saga.AssociationValue;
import org.axonframework.saga.Saga;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;

/**
 * Collects operations for {@link Saga} executions.
 *
 * @author Joris Kuipers
 * @since 2.0
 */
public aspect SagaOperationCollectionAspect extends MethodOperationCollectionAspect {

    public pointcut collectionPoint():
            execution(@org.axonframework.saga.annotation.SagaEventHandler * *(*, ..)) ||
                    (execution(void org.axonframework.saga.Saga.handle(*))
                            && !within(org.axonframework.saga.annotation.AbstractAnnotatedSaga));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Saga saga = (Saga) jp.getTarget();
        Operation operation = super.createOperation(jp)
                .type(AxonOperationType.SAGA)
                .put("id", saga.getSagaIdentifier());
        OperationMap values = operation.createMap("associationValues");
        for (AssociationValue value : saga.getAssociationValues()) {
            values.put(value.getKey(), value.getValue());
        }
        Object[] args = jp.getArgs();
        if (!AxonVersion.IS_AXON_1X) {
            Axon20OperationUtils.processEventMessage(args, operation);
            // we're done here
            return operation;
        }
        Object event = args[0];
        operation.put("eventType", event.getClass().getName());
        return operation;
    }

    @Override
    public final String getPluginName() {
        return AxonPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
