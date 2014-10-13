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
import org.axonframework.eventhandling.EventBus;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;

/**
 * {@link EventBus} publish operation matching for Axon 1.x apps.
 *
 * @author Joris Kuipers
 * @since 2.0
 */
public aspect EventBus1xPublishOperationCollectionAspect extends AbstractOperationCollectionAspect {

    public pointcut collectionPoint(): execution(* org.axonframework.eventhandling.EventBus.publish(!org.axonframework.domain.EventMessage));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object event = jp.getArgs()[0];
        String eventType = event.getClass().getName();
        Operation op = new Operation()
                .label("Axon EventBus Publish")
                .type(AxonOperationType.EVENT_BUS)
                .sourceCodeLocation(getSourceCodeLocation(jp))
                .put("eventType", eventType);
        return op;
    }

    @Override
    public final String getPluginName() {
        return AxonPluginRuntimeDescriptor.PLUGIN_NAME;
    }

}
