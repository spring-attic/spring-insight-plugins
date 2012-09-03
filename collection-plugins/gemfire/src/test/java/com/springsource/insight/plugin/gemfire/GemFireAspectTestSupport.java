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

package com.springsource.insight.plugin.gemfire;

import org.junit.Before;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

public abstract class GemFireAspectTestSupport extends OperationCollectionAspectTestSupport {
	private Cache cache;

	protected GemFireAspectTestSupport () {
		super();
	}

	@Before
	@Override
	public void setUp() {
		super.setUp();
    	cache = new CacheFactory()
        .set("name", "Test")
        .set("log-level", "warning")
        .set("cache-xml-file", "gemfire.xml")
        .create();    			
	}
	
    protected void testInGemfire(GemFireCallback callback, TestCallback test) {
    	Region r = cache.getRegion("test");
    	callback.doInGemfire(r);
        test.doTest(getLastEntered());    	
    }
    
    protected interface GemFireCallback {
    	void doInGemfire(Region region);
    }

    protected interface TestCallback {
    	void doTest(Operation operation);
    }
    

}
