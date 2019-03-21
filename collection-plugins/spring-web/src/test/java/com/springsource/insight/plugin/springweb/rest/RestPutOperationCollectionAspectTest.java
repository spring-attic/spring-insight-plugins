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

import java.net.URI;
import java.util.Collections;
import java.util.Date;

import org.junit.Test;


/**
 * 
 */
public class RestPutOperationCollectionAspectTest extends RestOperationCollectionTestSupport {
	public RestPutOperationCollectionAspectTest() {
		super();
	}

	@Test
	public void testPutNoArgs() throws Exception {
		opsInstance.put(new URI(createTestURI("testPutNoArgs")), this);
		assertRestOperationResult(opsInstance);
	}

	@Test
	public void testPutVarArgs() throws Exception {
		opsInstance.put(createTestURI("testPutVarArgs"), this, new Date());
		assertRestOperationResult(opsInstance);
	}

	@Test
	public void testPutArgsMap() throws Exception {
		opsInstance.put(createTestURI("testPutArgsMap"), this, Collections.singletonMap("now", new Date()));
		assertRestOperationResult(opsInstance);
	}

	@Override
	public RestPutOperationCollectionAspect getAspect() {
		return RestPutOperationCollectionAspect.aspectOf();
	}
}
