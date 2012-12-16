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

import java.util.HashMap;
import java.util.Map;

import com.springsource.insight.plugin.akka.routing.AkkaRouterConfigHelper;

import akka.actor.ActorRef;
import akka.routing.RoutedActorRef;
import akka.routing.RouterConfig;

class RouterActorRefPropertyExtractor implements ActorRefPropertyExtractor {

    public static final RouterActorRefPropertyExtractor INSTANCE = new RouterActorRefPropertyExtractor();

    private RouterActorRefPropertyExtractor() {
    	super();
    }

    public Map<String, Object> extractProperties(ActorRef actorRef) {
		RoutedActorRef ref = (RoutedActorRef) actorRef;
		Map<String, Object> result = new HashMap<String, Object>();
		RouterConfig routerConfig = ref.routerConfig();
		result.putAll(AkkaRouterConfigHelper.getInstance().getRouterConfigInformation(routerConfig));
		result.put("local", Boolean.valueOf(ref.isLocal()));
		return result;
    }

}
