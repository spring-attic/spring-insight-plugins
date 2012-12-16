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

import java.util.Map;

import org.aspectj.lang.JoinPoint;

import akka.actor.ActorRef;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.plugin.akka.actorref.ActorRefHelper;

/**
 * 
 */
public aspect ActorRefOperationCollectionAspect extends MethodOperationCollectionAspect {
	public ActorRefOperationCollectionAspect() {
		super();
	}

    public pointcut collectionPoint() : execution(public void ActorRef+.tell(Object)) || execution(public void ActorRef+.tell(Object, ActorRef));

    @Override
    protected Operation createOperation(final JoinPoint jp) {
	Object target = jp.getTarget();
	ActorRef actorRef = (ActorRef) target;
	Map<String, Object> additionalInfo = getAdditionalInfo(actorRef);
	String path = String.valueOf(actorRef.path());
	String actorRefType = target.getClass().getSimpleName();
	Class<?> messageClass = jp.getArgs()[0].getClass();
	return super.createOperation(jp)
		.type(AkkaDefinitions.OperationTypes.AKKA_OP_ACTOR_REF)
		.label(buildLabel(path, messageClass))
		.put(AkkaDefinitions.Labels.PATH, path)
		.put(AkkaDefinitions.Labels.MESSAGE, messageClass.getName())
		.put(AkkaDefinitions.Labels.ACTOR_REF, actorRefType)
		.putAnyAll(additionalInfo)
		;

    }

    private String buildLabel(String path, Class<?> messageClass) {
	return "ActorRef#tell(" + messageClass.getSimpleName() + ") sent to " + path;
    }

    private Map<String, Object> getAdditionalInfo(ActorRef actorRef) {
	return ActorRefHelper.getInstance().getActorRefProps(actorRef);
    }

    @Override
    public String getPluginName() {
	return AkkaPluginRuntimeDescriptor.PLUGIN_NAME;
    }

}
