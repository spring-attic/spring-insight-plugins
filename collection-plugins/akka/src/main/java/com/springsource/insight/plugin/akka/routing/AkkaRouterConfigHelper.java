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
import java.util.HashMap;
import java.util.Map;

import akka.routing.RoundRobinRouter;
import akka.routing.RouterConfig;

public final class AkkaRouterConfigHelper {

    private static final AkkaRouterConfigHelper INSTANCE = new AkkaRouterConfigHelper();

    public static final AkkaRouterConfigHelper getInstance() {
	return INSTANCE;
    }

    private Map<Class<? extends RouterConfig>, RouterConfigDataExtractor> routerConfigExtractors = new HashMap<Class<? extends RouterConfig>, RouterConfigDataExtractor>();

    private AkkaRouterConfigHelper() {
	routerConfigExtractors.put(RoundRobinRouter.class, RoundRobinRouterDataExtractor.INSTANCE);

    }

    public Map<String, String> getRouterConfigInformation(RouterConfig routerConfig) {
	Map<String, String> result = new HashMap<String, String>();
	result.put("Router", routerConfig.getClass().getSimpleName());
	String routerDispatcher = routerConfig.routerDispatcher();
	result.put("Router Dispatcher", routerDispatcher);
	result.putAll(extract(routerConfig));
	return result;
    }

    private Map<String, String> extract(RouterConfig routerConfig) {
	Class<?> searchClass = routerConfig.getClass();
	while (!Object.class.equals(searchClass)) {
	    RouterConfigDataExtractor extractor = routerConfigExtractors.get(searchClass);
	    if (extractor == null) {
		searchClass = searchClass.getSuperclass();
	    } else {
		return extractor.extract(routerConfig);
	    }
	}
	return Collections.emptyMap();
    }

}
