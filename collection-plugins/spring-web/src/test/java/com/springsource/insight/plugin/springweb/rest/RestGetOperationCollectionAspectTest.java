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
public class RestGetOperationCollectionAspectTest extends RestOperationCollectionTestSupport {
	public RestGetOperationCollectionAspectTest() {
		super();
	}

	@Test
	public void testGetForEntityNoArgs () throws Exception {
		assertRestOperationResult(opsInstance.getForEntity(new URI(createTestURI("testGetForEntityNoArgs")), Serializable.class));
	}

	@Test
	public void testGetForEntityVarArgs () throws Exception {
		assertRestOperationResult(opsInstance.getForEntity(createTestURI("testGetForEntityVarArgs"), Serializable.class, new Date()));
	}

	@Test
	public void testGetForEntityMappedVars () throws Exception {
		assertRestOperationResult(opsInstance.getForEntity(createTestURI("testGetForEntityMappedVars"), Serializable.class, Collections.singletonMap("now",  new Date())));
	}

	@Test
	public void testGetForObjectNoArgs () throws Exception {
		assertRestOperationResult(opsInstance.getForObject(new URI(createTestURI("testGetForObjectNoArgs")), Serializable.class));
	}

	@Test
	public void testGetForObjectVarArgs () throws Exception {
		assertRestOperationResult(opsInstance.getForObject(createTestURI("testGetForObjectVarArgs"), Serializable.class, new Date()));
	}

	@Test
	public void testGetForObjectMappedVars () throws Exception {
		assertRestOperationResult(opsInstance.getForObject(createTestURI("testGetForObjectMappedVars"), Serializable.class, Collections.singletonMap("now",  new Date())));
	}

	@Override
	public RestGetOperationCollectionAspect getAspect() {
		return RestGetOperationCollectionAspect.aspectOf();
	}
}
