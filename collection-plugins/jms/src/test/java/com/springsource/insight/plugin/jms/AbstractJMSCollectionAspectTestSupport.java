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

package com.springsource.insight.plugin.jms;

import java.util.Map;

import org.junit.After;
import org.junit.Before;

import com.springsource.insight.collection.ObscuredValueSetMarker;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.trace.ObscuredValueMarker;
import com.springsource.insight.util.ListUtil;

/**
 * 
 */
public abstract class AbstractJMSCollectionAspectTestSupport extends OperationCollectionAspectTestSupport {
	private ObscuredValueMarker	originalMarker;
	private final ObscuredValueSetMarker	replaceMarker=new ObscuredValueSetMarker();

	protected AbstractJMSCollectionAspectTestSupport() {
		super();
	}

    @Before
    @Override
    public void setUp() {
    	super.setUp();
    	
    	AbstractJMSCollectionAspect.OBFUSCATED_HEADERS.clear();
    	AbstractJMSCollectionAspect.OBFUSCATED_PROPERTIES.clear();

    	AbstractJMSCollectionAspect	aspectInstance=getJmsCollectionAspect();
    	originalMarker = aspectInstance.getSensitiveValueMarker();
    	replaceMarker.clear();
    	aspectInstance.setSensitiveValueMarker(replaceMarker);
    }

    @After
    @Override
    public void restore() {
    	AbstractJMSCollectionAspect	aspectInstance=getJmsCollectionAspect();
    	aspectInstance.setSensitiveValueMarker(originalMarker);
    	originalMarker = null;

    	AbstractJMSCollectionAspect.OBFUSCATED_HEADERS.clear();
    	AbstractJMSCollectionAspect.OBFUSCATED_PROPERTIES.clear();

    	super.restore();
    }

    protected void assertObfuscatedValuesState (Map<String,?> attrs, boolean obfuscated) {
    	AbstractJMSCollectionAspect	aspectInstance=getJmsCollectionAspect();
    	ObscuredValueSetMarker		markedObjects=(ObscuredValueSetMarker) aspectInstance.getSensitiveValueMarker();
    	if (!obfuscated) {
    		assertEquals("Unexpected obfuscated values: " + markedObjects, 0, ListUtil.size(markedObjects));
    		return;
    	}

    	for (Map.Entry<String,?> ae : attrs.entrySet()) {
    		String	key=ae.getKey();
    		Object	value=ae.getValue();
    		assertTrue("Value for key=" + key + " not marked as obfuscated", markedObjects.remove(value));
    	}
    	
    	assertTrue("Orphan obfuscated values: " + markedObjects, markedObjects.isEmpty());
    }

	public AbstractJMSCollectionAspect getJmsCollectionAspect() {
		return (AbstractJMSCollectionAspect) getAspect();
	}
}
