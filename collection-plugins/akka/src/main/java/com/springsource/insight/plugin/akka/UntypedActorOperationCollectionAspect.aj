/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
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

package com.springsource.insight.plugin.akka;

import org.aspectj.lang.JoinPoint;

import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public aspect UntypedActorOperationCollectionAspect extends MethodOperationCollectionAspect {

    public pointcut collectionPoint() : execution(public void UntypedActor+.onReceive(Object));

    @Override
    protected Operation createOperation(final JoinPoint jp) {
	Object target = jp.getTarget();
	UntypedActor actor = (UntypedActor) target;
	String actorType = target.getClass().getSimpleName();
	String system = actor.getSelf().path().address().system();
	ActorRef senderRef = actor.getSender();
	String senderPath = buildPath(senderRef);
	Class<?> messageClass = jp.getArgs()[0].getClass();
	return super.createOperation(jp).type(AkkaDefinitions.OperationTypes.AKKA_OP_UNTYPED_ACTOR)
		.label(buildLabel(actorType, messageClass))
		.put(AkkaDefinitions.Labels.ACTOR + AkkaDefinitions.Labels.PATH, buildPath(actor.getSelf()))
		.put(AkkaDefinitions.Labels.SYSTEM, system)
		.put(AkkaDefinitions.Labels.SENDER + AkkaDefinitions.Labels.PATH, senderPath)
		.put(AkkaDefinitions.Labels.MESSAGE, messageClass.getName())
		.put(AkkaDefinitions.Labels.ACTOR, actorType);

    }

    private String buildLabel(String actorType, Class<?> messageClass) {
	return actorType + "#onReceive(" + messageClass.getSimpleName() + ")";
    }

    private String buildPath(ActorRef ref) {
	ActorPath path = ref.path();
	return String.valueOf(path);
    }

    @Override
    public String getPluginName() {
	return AkkaPluginRuntimeDescriptor.PLUGIN_NAME;
    }

    @Override
    public boolean isEndpoint() {
	return true;
    }

}
