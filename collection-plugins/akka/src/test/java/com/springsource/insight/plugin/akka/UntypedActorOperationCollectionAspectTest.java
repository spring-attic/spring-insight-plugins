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
import java.util.Map.Entry;

import org.junit.Test;

import akka.actor.ActorRef;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.intercept.operation.Operation;

public class UntypedActorOperationCollectionAspectTest extends AbstractAkkaOperationCollectionAspectTestSupport {
	public UntypedActorOperationCollectionAspectTest() {
		super();
	}

    @Test
    public void testCreateOperation() throws Exception {
	ActorRef ref = createActorRef();
	ref.tell(getClass());

	Thread.sleep(1000);
	
	Operation operation = getLastEntered();
	Operation expected = new Operation().type(AkkaDefinitions.OperationTypes.AKKA_OP_UNTYPED_ACTOR)
		.put(AkkaDefinitions.Labels.MESSAGE, Class.class.getName())
		.label(getUntypedActorClass().getSimpleName() + "#onReceive(" + Class.class.getSimpleName() + ")")
		.put(AkkaDefinitions.Labels.SYSTEM, actorSystem.name())
		.put(AkkaDefinitions.Labels.ACTOR, getUntypedActorClass().getSimpleName())
		.put(AkkaDefinitions.Labels.ACTOR + AkkaDefinitions.Labels.PATH, getActorRefPath())
		.put(AkkaDefinitions.Labels.SENDER + AkkaDefinitions.Labels.PATH, getDeadLettersPath());
	assertUntypedActorOperation(expected, operation);
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
	return UntypedActorOperationCollectionAspect.aspectOf();
    }

    private void assertUntypedActorOperation(Operation expected, Operation operation) {
	Map<String, Object> map = operation.asMap();
	for (Entry<String, Object> expectedEntry : expected.asMap().entrySet()) {
	    String key = expectedEntry.getKey();
	    Object resultValue = map.get(key);
	    if (resultValue == null || !expectedEntry.getValue().equals(resultValue)) {
		fail("expcted key-value '" + expectedEntry + "' but got '" + key + "=" + resultValue + "'");
	    }
	}
    }

}
