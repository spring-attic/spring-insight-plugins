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
import org.springframework.stereotype.Repository;

public class RepositoryMethodOperationCollectionAspectTest extends StereotypeOperationCollectionAspectTestSupport {
    // This test focuses only on selection, since all that 
    // RepositoryMethodOperationCollectionAspect contains is a pointcut. 
    // Tests for return value, exceptions etc. reside in MethodOperationCollectionAspectTest
    
    @Test
    public void repositoryCollected() {
        ExampleRepository repository = new ExampleRepository();
        repository.perform();
        
        assertStereotypeOperation(ExampleRepository.class, "perform");
    }

    @Repository
    private static class ExampleRepository {
    	public ExampleRepository () {
    		super();
    	}

        public void perform() {
            System.out.println("test");
        }
    }
}
