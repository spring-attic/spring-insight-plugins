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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;


/**
 *
 */
public class RestExchangeOperationCollectionAspectTest extends RestOperationCollectionTestSupport {
    public RestExchangeOperationCollectionAspectTest() {
        super();
    }

    @Test
    public void testExchangeNoArgs() throws Exception {
        assertRestOperationResult(opsInstance.exchange(new URI(createTestURI("testExchangeNoArgs")), HttpMethod.GET, HttpEntity.EMPTY, Serializable.class));
    }

    @Test
    public void testExchangeVarArgs() throws Exception {
        assertRestOperationResult(opsInstance.exchange(createTestURI("testExchangeVarArgs"), HttpMethod.POST, HttpEntity.EMPTY, Serializable.class, new Date()));
    }

    @Test
    public void testExchangeMappedVars() throws Exception {
        assertRestOperationResult(opsInstance.exchange(createTestURI("testExchangeMappedVars"), HttpMethod.DELETE, HttpEntity.EMPTY, Serializable.class, Collections.singletonMap("now", new Date())));
    }

    @Override
    public RestExchangeOperationCollectionAspect getAspect() {
        return RestExchangeOperationCollectionAspect.aspectOf();
    }
}
