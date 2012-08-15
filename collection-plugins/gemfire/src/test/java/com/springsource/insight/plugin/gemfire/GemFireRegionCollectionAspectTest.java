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

import org.junit.Test;

import com.gemstone.bp.edu.emory.mathcs.backport.java.util.Collections;
import com.gemstone.gemfire.cache.Region;
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.intercept.operation.Operation;

public class GemFireRegionCollectionAspectTest extends GemFireAspectTestSupport {
    	
	private final TestCallback test = new TestCallback() {			
			public void doTest(Operation operation) {
				assertEquals("/test", operation.get(GemFireDefenitions.FIELD_PATH));
			}
		};

	public GemFireRegionCollectionAspectTest () {
		super();
	}

    @Test
    public void getOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region r) {
				r.get("a");
			}
		}, test);
    }

    @Test
    public void getAllOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region r) {
				r.getAll(Collections.singleton("a"));
			}
		}, test);
    }
    
    @Test
    public void getEntryOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region r) {
				r.getEntry("a");
			}
		}, test);
    }
    
    @Test
    public void selectOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region r) {
				try {
					r.selectValue("length > 1");
				} catch (Exception e) {
					e.printStackTrace();
					org.junit.Assert.fail();
				}
			}
		}, test);
    }

    @Test
    public void valuesOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region r) {
				r.values();
			}
		}, test);
    }
    
    @Test
    public void putOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region r) {
				r.put("a", "b");
			}
		}, test);
    }
    
    @Test
    public void putAllOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region r) {
				r.putAll(Collections.singletonMap("a", "b'"));
			}
		}, test);
    }
    
    @Test
    public void queryOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region r) {
				try {
					r.query("length > 1");
				} catch (Exception e) {
					e.printStackTrace();
					org.junit.Assert.fail();
				}
			}
		}, test);
    }
    
    @Test
    public void removeOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region r) {
				r.remove("a");
			}
		}, test);
    }
        
    @Test
    public void clearOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region r) {
				r.clear();
			}
		}, test);
    }
    
    @Test
    public void containsKeyOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region r) {
				r.containsKey("a");
			}
		}, test);
    }
    
    @Test
    public void containsValueOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region r) {
				r.containsValue("a");
			}
		}, test);
    }
    
    @Test(expected=com.gemstone.gemfire.cache.EntryNotFoundException.class)
    public void destroyOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region r) {
				r.destroy("a");
			}
		}, test);
    }
    
    @Test
    public void existsValueOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region r) {
				try {
					r.query("length > 1");
				} catch (Exception e) {
					e.printStackTrace();
					org.junit.Assert.fail();
				}
			}
		}, test);
    }
    
    @Test
    public void isEmptyOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region r) {
				r.isEmpty();
			}
		}, test);
    }

    @Test(expected=com.gemstone.gemfire.cache.EntryNotFoundException.class)
    public void invalidateOperationCollection() {
    	testInGemfire(new GemFireCallback() {			
			public void doInGemfire(Region r) {
				r.invalidate("a");
			}
		}, test);
    }
    
    
    @Override
	public OperationCollectionAspectSupport getAspect() {
		return GemFireRegionCollectionAspect.aspectOf();
	}
}