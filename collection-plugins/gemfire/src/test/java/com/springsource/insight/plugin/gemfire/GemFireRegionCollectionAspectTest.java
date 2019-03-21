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

import java.util.Collections;

import org.junit.Test;

import com.gemstone.gemfire.cache.Region;
import com.springsource.insight.intercept.operation.Operation;

public class GemFireRegionCollectionAspectTest extends GemFireAspectTestSupport {
    	
	private static final TestCallback testCallback = new TestCallback() {			
			public void doTest(Operation operation) {
				assertEquals("/test", operation.get(GemFireDefenitions.FIELD_PATH));
			}
		};

	public GemFireRegionCollectionAspectTest () {
		super();
	}

    @Test
    public void testGetOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region<?,?> r) {
				r.get("testGetOperationCollection");
			}
		}, testCallback);
    }

    @Test
    public void testGetAllOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region<?,?> r) {
				r.getAll(Collections.singleton("testGetAllOperationCollection"));
			}
		}, testCallback);
    }
    
    @Test
    public void testGetEntryOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region<?,?> r) {
				r.getEntry("testGetEntryOperationCollection");
			}
		}, testCallback);
    }
    
    @Test
    public void testSelectOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region<?,?> r) {
				try {
					/*
					 * NOTE: Make sure at most 1 result is returned (actually we expect zero...)
					 * otherwise an exception is thrown
					 */
					Object	result=r.selectValue("length > " + Short.MAX_VALUE);
					if (result != null) {
						System.out.println("testSelectOperationCollection: " + result);
					}
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			}
		}, testCallback);
    }

    @Test
    public void testValuesOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region<?,?> r) {
				r.values();
			}
		}, testCallback);
    }
    
    @Test
    public void testPutOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			@SuppressWarnings("unchecked")
			public void doInGemfire(Region<?,?> r) {
				((Region<Object,Object>) r).put("testPutOperationCollection", r.getClass().getSimpleName());
			}
		}, testCallback);
    }
    
    @Test
    public void testPutAllOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			@SuppressWarnings("unchecked")
			public void doInGemfire(Region<?,?> r) {
				((Region<Object,Object>) r).putAll(Collections.<Object,Object>singletonMap("testPutAllOperationCollection", r.getClass().getSimpleName()));
			}
		}, testCallback);
    }
    
    @Test
    public void testQueryOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region<?,?> r) {
				try {
					r.query("length > 1");
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getClass().getSimpleName() + ": " + e.getMessage());
				}
			}
		}, testCallback);
    }
    
    @Test
    public void testRemoveOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region<?,?> r) {
				r.remove("testRemoveOperationCollection");
			}
		}, testCallback);
    }
        
    @Test
    public void testClearOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region<?,?> r) {
				r.clear();
			}
		}, testCallback);
    }
    
    @Test
    public void testContainsKeyOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region<?,?> r) {
				r.containsKey("testContainsKeyOperationCollection");
			}
		}, testCallback);
    }
    
    @Test
    public void testContainsValueOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region<?,?> r) {
				r.containsValue("testContainsValueOperationCollection");
			}
		}, testCallback);
    }
    
    @Test(expected=com.gemstone.gemfire.cache.EntryNotFoundException.class)
    public void testDestroyOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region<?,?> r) {
				r.destroy("testDestroyOperationCollection");
			}
		}, testCallback);
    }
    
    
    @Test
    public void testIsEmptyOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region<?,?> r) {
				r.isEmpty();
			}
		}, testCallback);
    }

    @Test(expected=com.gemstone.gemfire.cache.EntryNotFoundException.class)
    public void testInvalidateOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region<?,?> r) {
				r.invalidate("testInvalidateOperationCollection");
			}
		}, testCallback);
    }
   
    @Override
	public GemFireRegionCollectionAspect getAspect() {
		return GemFireRegionCollectionAspect.aspectOf();
	}
}