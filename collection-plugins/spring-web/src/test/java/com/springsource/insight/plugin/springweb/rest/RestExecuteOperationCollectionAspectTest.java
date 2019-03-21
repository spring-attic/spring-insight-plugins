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
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;


/**
 * 
 */
public class RestExecuteOperationCollectionAspectTest extends RestOperationCollectionTestSupport {
	private final RequestCallback 		cb=Mockito.mock(RequestCallback.class);
	private final ResponseExtractor<?>	ex=Mockito.mock(ResponseExtractor.class);

	public RestExecuteOperationCollectionAspectTest() {
		super();
	}

	@Test
	public void testExecuteNoArgs () throws Exception {
		assertRestOperationResult(opsInstance.execute(new URI(createTestURI("testExecuteNoArgs")), HttpMethod.GET, cb, ex));
	}

	@Test
	public void testExecuteVarArgs () throws Exception {
		assertRestOperationResult(opsInstance.execute(createTestURI("testExecuteVarArgs"), HttpMethod.POST, cb, ex, new Date()));
	}

	@Test
	public void testExecuteMappedVars () throws Exception {
		assertRestOperationResult(opsInstance.execute(createTestURI("testExecuteMappedVars"), HttpMethod.DELETE, cb, ex, Collections.singletonMap("now",  new Date())));
	}

	@Override
	public RestExecuteOperationCollectionAspect getAspect() {
		return RestExecuteOperationCollectionAspect.aspectOf();
	}
}
