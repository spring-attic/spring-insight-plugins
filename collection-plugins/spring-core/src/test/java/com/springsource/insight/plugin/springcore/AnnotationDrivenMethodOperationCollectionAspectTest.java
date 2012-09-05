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

package com.springsource.insight.plugin.springcore;

import org.junit.Test;

import com.foo.example.ExampleComponent;
import com.foo.example.ExampleRepository;
import com.foo.example.ExampleService;

import com.springsource.insight.collection.method.MethodOperationsCollected;
import com.springsource.insight.collection.test.AbstractCollectionTestSupport;
import com.springsource.insight.plugin.springcore.beans.InsightComponent;
import com.springsource.insight.plugin.springcore.beans.InsightRepository;
import com.springsource.insight.plugin.springcore.beans.InsightService;

public class AnnotationDrivenMethodOperationCollectionAspectTest extends AbstractCollectionTestSupport {
	public AnnotationDrivenMethodOperationCollectionAspectTest () {
		super();
	}

    @Test
    public void testMethodOperationsCollectedAnnotationAppliedCorrectly() {
    	assertIsMethodOperationsCollected(true, ExampleComponent.class, ExampleService.class, ExampleRepository.class);
    	// should not be annotated since inside the Insight packages
    	assertIsMethodOperationsCollected(false, InsightComponent.class, InsightService.class, InsightRepository.class);
    }

    private static void assertIsMethodOperationsCollected (boolean expected, Class<?> ... classes) {
    	for (Class<?> clazz : classes) {
    		assertIsMethodOperationsCollected(clazz, expected);	
    	}
    }

    private static void assertIsMethodOperationsCollected (Class<?> clazz, boolean expected) {
    	assertEquals(clazz.getSimpleName() + " annotation mismatch",
    				 Boolean.valueOf(expected), Boolean.valueOf(clazz.isAnnotationPresent(MethodOperationsCollected.class)));
    }
}
