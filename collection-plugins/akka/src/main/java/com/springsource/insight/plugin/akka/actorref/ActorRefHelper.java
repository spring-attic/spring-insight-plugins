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
package com.springsource.insight.plugin.akka.actorref;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import akka.actor.ActorRef;
import akka.pattern.PromiseActorRef;
import akka.routing.RoutedActorRef;

public final class ActorRefHelper {

    private static final ActorRefHelper INSTANCE = new ActorRefHelper();

    public static ActorRefHelper getInstance() {
        return INSTANCE;
    }

    private Map<Class<? extends ActorRef>, ActorRefPropertyExtractor> extractors = new HashMap<Class<? extends ActorRef>, ActorRefPropertyExtractor>();

    private ActorRefHelper() {
        extractors.put(RoutedActorRef.class, RouterActorRefPropertyExtractor.INSTANCE);
        extractors.put(PromiseActorRef.class, PromiseActorRefPropertyExtractor.INSTANCE);
    }

    public Map<String, Object> getActorRefProps(ActorRef actorRef) {
        return extract(actorRef);
    }

    private Map<String, Object> extract(ActorRef actorRef) {
        Class<?> searchClass = actorRef.getClass();
        while (!Object.class.equals(searchClass)) {
            ActorRefPropertyExtractor extractor = extractors.get(searchClass);
            if (extractor == null) {
                searchClass = searchClass.getSuperclass();
            } else {
                return extractor.extractProperties(actorRef);
            }
        }
        return Collections.emptyMap();
    }

}
