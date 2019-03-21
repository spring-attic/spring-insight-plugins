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

package com.springsource.insight.plugin.springcore;

import org.junit.Test;
import org.springframework.stereotype.Service;

import com.foo.example.ExampleService;
import com.springsource.insight.plugin.springcore.beans.InsightService;

public class ServiceMethodOperationCollectionAspectTest extends StereotypeOperationCollectionAspectTestSupport {
    public ServiceMethodOperationCollectionAspectTest() {
        super(Service.class);
    }

    @Test
    public void testNonInsightComponentCollected() {
        assertStereotypeOperation(new ExampleService(), true);
    }

    @Test
    public void testInsightComponentNotCollected() {
        assertStereotypeOperation(new InsightService(), false);
    }

    @Test
    public void testLifecycleMethodsNotCollected() throws Exception {
        assertLifecycleMethodsNotIntercepted(new ExampleService());
    }

    @Override
    public ServiceMethodOperationCollectionAspect getAspect() {
        return ServiceMethodOperationCollectionAspect.aspectOf();
    }
}
