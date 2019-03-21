/**
 * Copyright (c) 2010-2012 Axon Framework All Rights Reserved.
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
package org.axonframework.insight.plugin.axon;

import org.aspectj.lang.JoinPoint;
import org.axonframework.commandhandling.CommandMessage;

import com.springsource.insight.collection.AbstractOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;

/**
 * {@link org.axonframework.commandhandling.CommandBus} dispatch operation matching for Axon 2.0 apps.
 *
 * @author Joris Kuipers
 * @since 2.0
 */
public aspect CommandBus20DispatchOperationCollectionAspect extends AbstractOperationCollectionAspect {

    public pointcut collectionPoint(): execution(* org.axonframework.commandhandling.CommandBus.dispatch(CommandMessage, ..));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[] args = jp.getArgs();
        CommandMessage<?> message = (CommandMessage<?>) args[0];
        String commandType = message.getPayloadType().getName();
        Operation op = new Operation()
                .label("Axon CommandBus Dispatch")
                .type(AxonOperationType.COMMAND_BUS)
                .sourceCodeLocation(getSourceCodeLocation(jp))
                .put("commandType", commandType)
                .put("commandId", message.getIdentifier());
        if (args.length == 2) {
            op.put("callbackType", args[1].getClass().getName());
        }
        Axon20OperationUtils.addMetaDataTo(op, message);
        return op;
    }

    @Override
    public final String getPluginName() {
        return AxonPluginRuntimeDescriptor.PLUGIN_NAME;
    }


}
