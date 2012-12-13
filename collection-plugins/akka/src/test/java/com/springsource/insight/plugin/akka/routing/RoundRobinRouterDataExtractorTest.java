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
package com.springsource.insight.plugin.akka.routing;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import akka.routing.RoundRobinRouter;

public class RoundRobinRouterDataExtractorTest {

    @Test
    public void testExtract() {
	RoundRobinRouter roundRobinRouter = new RoundRobinRouter(5);
	RoundRobinRouterDataExtractor extractor = RoundRobinRouterDataExtractor.INSTANCE;
	
	Map<String, String> extracted = extractor.extract(roundRobinRouter);
	Map<String, String> expected = Collections.singletonMap("Num of instances", String.valueOf(5));
	
	assertEquals("extracted property maps" , expected, extracted);
	
    }

}
