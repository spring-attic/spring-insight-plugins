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

package com.springsource.insight.plugin.springweb.rest;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.Date;

import org.junit.Test;


/**
 * 
 */
public class RestPostOperationCollectionAspectTest extends RestOperationCollectionTestSupport {
	public RestPostOperationCollectionAspectTest() {
		super();
	}

	@Test
	public void testPostForLocationNoArgs() throws Exception {
		assertRestOperationResult(opsInstance.postForLocation(new URI(createTestURI("testPostForLocationNoArgs")), this));
	}

	@Test
	public void testPostForLocationVarArgs() throws Exception {
		assertRestOperationResult(opsInstance.postForLocation(createTestURI("testPostForLocationVarArgs"), this, new Date()));
	}

	@Test
	public void testPostForLocationArgsMap() throws Exception {
		assertRestOperationResult(opsInstance.postForLocation(createTestURI("testHeadForHeadersArgsMap"), this, Collections.singletonMap("now", new Date())));
	}

	@Test
	public void testPostForEntityNoArgs() throws Exception {
		assertRestOperationResult(opsInstance.postForEntity(new URI(createTestURI("testPostForEntityNoArgs")), this, Serializable.class));
	}

	@Test
	public void testPostForEntityVarArgs() throws Exception {
		assertRestOperationResult(opsInstance.postForEntity(createTestURI("testPostForEntityVarArgs"), this, Serializable.class, new Date()));
	}

	@Test
	public void testPostForEntityArgsMap() throws Exception {
		assertRestOperationResult(opsInstance.postForEntity(createTestURI("testPostForEntityArgsMap"), this, Serializable.class, Collections.singletonMap("now", new Date())));
	}

	@Test
	public void testPostForObjectNoArgs() throws Exception {
		assertRestOperationResult(opsInstance.postForObject(new URI(createTestURI("testPostForObjectNoArgs")), this, Serializable.class));
	}

	@Test
	public void testPostForObjectVarArgs() throws Exception {
		assertRestOperationResult(opsInstance.postForObject(createTestURI("testPostForObjectVarArgs"), this, Serializable.class, new Date()));
	}

	@Test
	public void testPostForObjectArgsMap() throws Exception {
		assertRestOperationResult(opsInstance.postForObject(createTestURI("testPostForObjectArgsMap"), this, Serializable.class, Collections.singletonMap("now", new Date())));
	}

	@Override
	public RestPostOperationCollectionAspect getAspect() {
		return RestPostOperationCollectionAspect.aspectOf();
	}
}
