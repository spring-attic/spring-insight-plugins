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

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import akka.routing.RoutedActorRef;

public class RouterActorRefPropertyExtractorTest {

    private RoutedActorRef tested;

    @Before
    public void setUp() {
	tested = mock(RoutedActorRef.class, Mockito.RETURNS_MOCKS);
    }

    @Test
    public void testExtractProperties() {
	Map<String, Object> result = RouterActorRefPropertyExtractor.INSTANCE.extractProperties(tested);
	assertKeysExist(Arrays.asList("local", "Router Dispatcher", "Router"), result.keySet());
    }

    private void assertKeysExist(List<String> expectedKeys, Collection<String> keys) {

	for (String key : expectedKeys) {
	    if (!keys.contains(key)) {
		fail("key '" + key + "' doesn't exist in result " + keys);
	    }
	}
    }

}
