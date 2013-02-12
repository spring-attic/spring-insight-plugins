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

import il.co.springsource.insight.MyApplicationEventMulticaster;
import il.co.springsource.insight.MyApplicationEventPublisher;
import il.co.springsource.insight.MyEvent;

import org.junit.Test;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public class EventPublisingOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	public EventPublisingOperationCollectionAspectTest() {
		super();
	}

	@Test
	public void testPublishEvent () {
		MyApplicationEventPublisher	publisher=new MyApplicationEventPublisher();
		MyEvent	event=new MyEvent("testPublishEvent");
		publisher.publishEvent(event);

		assertPublishOperation("publish");
		assertSame("Mismatched event instance", event, publisher.getLastEvent());
	}

	@Test
	public void testMulticastEvent () {
		MyApplicationEventMulticaster	publisher=new MyApplicationEventMulticaster();
		MyEvent	event=new MyEvent("testMulticastEvent");
		publisher.multicastEvent(event);

		assertPublishOperation("multicast");
		assertSame("Mismatched event instance", event, publisher.getLastEvent());
	}

	protected Operation assertPublishOperation(String expectedAction) {
		Operation	op=getLastEntered();
		assertNotNull(expectedAction + ": no operation", op);
		assertEquals(expectedAction + ": mismatched type", SpringCorePluginRuntimeDescriptor.EVENT_PUBLISH_TYPE, op.getType());

		String	compType=op.get(StereotypedSpringBeanMethodOperationCollectionAspectSupport.COMP_TYPE_ATTR, String.class);
        // make sure not intercepted by one of the stereotyped beans aspects
        assertNull(expectedAction + ": Unexpected stereotyped bean method collection: " + compType, compType);
        assertEquals("Mismatched action", expectedAction, op.get(EventPublisingOperationCollectionAspect.ACTION_ATTR, String.class));
        
        assertEquals(expectedAction + ": mismatched event type",
   		     		 MyEvent.class.getName(), op.get(SpringLifecycleMethodOperationCollectionAspect.EVENT_ATTR, String.class));
        return op;
	}

	@Override
	public EventPublisingOperationCollectionAspect getAspect() {
		return EventPublisingOperationCollectionAspect.aspectOf();
	}
}
