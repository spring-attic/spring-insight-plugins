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

package com.springsource.insight.plugin.akka.routing;

import java.util.Collections;
import java.util.Map;

import akka.routing.RoundRobinRouter;
import akka.routing.RouterConfig;

public class RoundRobinRouterDataExtractor implements RouterConfigDataExtractor {

    public static final RoundRobinRouterDataExtractor INSTANCE = new RoundRobinRouterDataExtractor();

    private RoundRobinRouterDataExtractor() {
    }

    public Map<String, String> extract(RouterConfig routerConfig) {
	RoundRobinRouter router = (RoundRobinRouter) routerConfig;
	return Collections.singletonMap("Num of instances", String.valueOf(router.nrOfInstances()));
    }
}
