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
package com.springsource.insight.plugin.akka;

import org.junit.After;
import org.junit.Before;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;

public abstract class AbstractAkkaOperationCollectionAspectTestSupport extends OperationCollectionAspectTestSupport {

    protected static final String AKKA_PREFIX = "akka://";
    protected static final String TEST_SYSTEM_NAME = "test-system";
    protected static final String TEST_ACTOR_NAME = "test-actor";
    protected ActorSystem actorSystem;
    protected ActorRef actorRef;

    @Before
    public final void initAkka() {
	actorSystem = ActorSystem.create(TEST_SYSTEM_NAME);
    }

    @After
    public final void closeAkka() {
	actorSystem.shutdown();
    }

    protected final ActorRef createActorRef() {
	actorRef = actorSystem.actorOf(new Props(SimpleUntypedActor.class), TEST_ACTOR_NAME);
	return actorRef;
    }

    protected final Class<? extends Actor> getUntypedActorClass() {
	return SimpleUntypedActor.class;
    }

    protected String getSystemPath() {
	return AKKA_PREFIX + TEST_SYSTEM_NAME;
    }

    protected String getDeadLettersPath() {
	return getSystemPath() + "/deadLetters";
    }

    protected String getActorRefPath() {
	return getSystemPath() + "/user/" + TEST_ACTOR_NAME;
    }

    public static final class SimpleUntypedActor extends UntypedActor {

	@Override
	public void onReceive(Object o) throws Exception {
	    // do nothing
	}

    }

}
