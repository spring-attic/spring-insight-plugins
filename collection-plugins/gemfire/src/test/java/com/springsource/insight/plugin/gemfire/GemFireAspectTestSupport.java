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

package com.springsource.insight.plugin.gemfire;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.mockito.ArgumentCaptor;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

public abstract class GemFireAspectTestSupport extends OperationCollectionAspectTestSupport {

	private Cache cache;

	@Before
	public void setup() {
    	cache = new CacheFactory()
        .set("name", "Test")
        .set("log-level", "warning")
        .set("cache-xml-file", "gemfire.xml")
        .create();    			
	}
	
    protected void testInGemfire(GemFireCallback callback, TestCallback test) {
    	Region r = cache.getRegion("test");
    	callback.doInGemfire(r);
    	
        ArgumentCaptor<Operation> operationCaptor = ArgumentCaptor.forClass(Operation.class);
        verify(spiedOperationCollector).enter(operationCaptor.capture());
        Operation operation = operationCaptor.getValue();
        operation.finalizeConstruction();
        
        test.doTest(operation);    	
    }
    
    protected interface GemFireCallback {
    	void doInGemfire(Region region);
    }

    protected interface TestCallback {
    	void doTest(Operation operation);
    }
    

}
