/**
 * Copyright 2009-2011 the original author or authors.
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
import org.springframework.stereotype.Service;

import static org.junit.Assert.*;

import com.springsource.insight.collection.method.MethodOperationsCollected;

public class AnnotationDrivenMethodOperationCollectionAspectTest {

    @Test
    public void methodOperationsCollectedAnnotationAppliedCorrectly() {
        Class<ExampleRepositoryThatIsAlsoService> exampleDualClass = ExampleRepositoryThatIsAlsoService.class;
        assertTrue(exampleDualClass.isAnnotationPresent(MethodOperationsCollected.class));

        Class<ExampleService> exampleServiceClass = ExampleService.class;
        assertTrue(exampleServiceClass.isAnnotationPresent(MethodOperationsCollected.class));

        Class<ExampleRepository> exampleRepositoryClass = ExampleRepository.class;
        assertTrue(exampleRepositoryClass.isAnnotationPresent(MethodOperationsCollected.class));
    }
    
    @Service
    private static class ExampleService {
        public void service() {
            
        }
    }
    
    @Repository
    private static class ExampleRepository {
        public void update() {
            
        }
    }
    
    @Service
    @Repository
    private static class ExampleRepositoryThatIsAlsoService {
        public void update() {
            
        }
    }
}
