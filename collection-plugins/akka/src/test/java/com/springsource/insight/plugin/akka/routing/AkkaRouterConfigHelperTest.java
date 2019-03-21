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

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.routing.CustomRoute;
import akka.routing.CustomRouterConfig;
import akka.routing.RouteeProvider;
import akka.routing.RouterConfig;

import com.springsource.insight.util.ExtraReflectionUtils;
import com.springsource.insight.util.ReflectionUtils;

public class AkkaRouterConfigHelperTest {

    private static final String TEST_DISPATCHER = "test-dispatcher";
    private static final Map<String, String> ROUTER_CONFIG_KEY_VALUE = Collections.singletonMap("test-key",
	    "test-value");
    private AkkaRouterConfigHelper tested;

    public AkkaRouterConfigHelperTest() {
    	super();
    }

    @Before
    public void setUp() {
	tested = AkkaRouterConfigHelper.getInstance();
	Field field = ExtraReflectionUtils.getAccessibleField(AkkaRouterConfigHelper.class, "routerConfigExtractors");
	ReflectionUtils.setField(field, tested,
		Collections.singletonMap(CustomRouterConfig.class, new RouterConfigDataExtractor() {

		    @SuppressWarnings("synthetic-access")
			public Map<String, String> extract(RouterConfig routerConfig) {
			return ROUTER_CONFIG_KEY_VALUE;
		    }
		}));
    }

    @Test
    public void testGetRouterConfigInformation() {
	Map<String, String> result = tested.getRouterConfigInformation(new TestCustomRouterConfig());
	Map<String, String> expected = createdExpected();
	assertEquals("extracted values", expected, result);
    }

    private Map<String, String> createdExpected() {
	Map<String, String> expected = new HashMap<String, String>();
	expected.put("Router", TestCustomRouterConfig.class.getSimpleName());
	expected.put("Router Dispatcher", TEST_DISPATCHER);
	expected.putAll(ROUTER_CONFIG_KEY_VALUE);
	return expected;
    }

    private static final class TestCustomRouterConfig extends CustomRouterConfig {
    public TestCustomRouterConfig() {
    	super();
    }

	public SupervisorStrategy supervisorStrategy() {
	    return null;
	}

	public String routerDispatcher() {
	    return TEST_DISPATCHER;
	}

	@Override
	public CustomRoute createCustomRoute(Props arg0, RouteeProvider arg1) {
	    return null;
	}
    }

}
