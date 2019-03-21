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

package com.springsource.insight.plugin.gemfire;

import org.junit.Test;

import com.gemstone.gemfire.cache.Region;
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.intercept.operation.Operation;

public class GemFireQueryCollectionAspectTest extends GemFireAspectTestSupport {

    public GemFireQueryCollectionAspectTest () {
    	super();
    }
	
    @Test
    public void putOperationCollection() throws Exception {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region r) {
				try {
					r.query("/test.size > 1");
				} catch (Exception e) {
					e.printStackTrace();
					org.junit.Assert.fail();
				}
			}
		}, new TestCallback() {			
			public void doTest(Operation operation) {
				assertEquals("select * from /test this where /test.size > 1", operation.get(GemFireDefenitions.FIELD_QUERY));
			}
		});
    }
        
	@Override
	public OperationCollectionAspectSupport getAspect() {
		return GemFireQueryCollectionAspect.aspectOf();
	}

}
