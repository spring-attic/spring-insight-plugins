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

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import org.aspectj.lang.JoinPoint;

/**
 * Collects operations for command handler executions.
 *
 * @author Joris Kuipers
 * @since 2.0
 */
public aspect CommandHandlerOperationCollectionAspect extends MethodOperationCollectionAspect {

    public pointcut collectionPoint():
            execution(@org.axonframework.commandhandling.annotation.CommandHandler * *(*, ..)) ||
                    (execution(* org.axonframework.commandhandling.CommandHandler.handle(*, org.axonframework.unitofwork.UnitOfWork))
                            && !within(org.axonframework.commandhandling.annotation.AnnotationCommandHandlerAdapter));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Operation operation = super.createOperation(jp).type(AxonOperationType.COMMAND_HANDLER);
        Object[] args = jp.getArgs();
        if (!AxonVersion.IS_AXON_1X) {
            if (Axon20OperationUtils.processCommandMessage(args, operation)) {
                // we're done here
                return operation;
            }
        }
        Object command = args[0];
        operation.put("commandType", command.getClass().getName());
        return operation;
    }

    @Override
    public final String getPluginName() {
        return AxonPluginRuntimeDescriptor.PLUGIN_NAME;
    }

}
