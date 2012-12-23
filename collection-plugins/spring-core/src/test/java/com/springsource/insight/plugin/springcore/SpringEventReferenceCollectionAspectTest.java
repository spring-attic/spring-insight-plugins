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

import il.co.springsource.insight.MyEvent;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import com.springsource.insight.collection.test.AbstractCollectionTestSupport;

/**
 * 
 */
public class SpringEventReferenceCollectionAspectTest extends AbstractCollectionTestSupport {
	public SpringEventReferenceCollectionAspectTest() {
		super();
	}

	@Test
	public void testNonApplicationContextEvent() {
		MyEvent	event=new MyEvent("testNonApplicationContextEvent");
		assertNullValue("Unexpected initial context", SpringEventReferenceCollectionAspect.getApplicationContext(event));
		assertTrue("Event class not marked without context", SpringEventReferenceCollectionAspect.nonContextEvents.contains(event.getClass()));
		assertNullValue("Unexpected extraction method mapping", SpringEventReferenceCollectionAspect.contextMethods.get(event.getClass()));
	}

	@Test
	public void testApplicationContextEvent() {
		ApplicationContext		expected=Mockito.mock(ApplicationContext.class);
		ContextRefreshedEvent	event=new ContextRefreshedEvent(expected);
		ApplicationContext		actual=SpringEventReferenceCollectionAspect.getApplicationContext(event);
		assertSame("Mismatched context instances", expected, actual);
		assertNotNull("Missing extraction method mapping", SpringEventReferenceCollectionAspect.contextMethods.get(event.getClass()));
		assertFalse("Event class marked as without context", SpringEventReferenceCollectionAspect.nonContextEvents.contains(event.getClass()));
	}

	@Test
	public void testStaticContextEventMethod() {
		StaticContextMethodEvent	event=new StaticContextMethodEvent();
		assertNullValue("Unexpected static context", SpringEventReferenceCollectionAspect.getApplicationContext(event));
		assertTrue("Event class not marked without context", SpringEventReferenceCollectionAspect.nonContextEvents.contains(event.getClass()));
		assertNullValue("Unexpected extraction method mapping", SpringEventReferenceCollectionAspect.contextMethods.get(event.getClass()));
	}

	@Test
	public void testExceptionInContextRetrieval() {
		ExceptionContextEvent	event=new ExceptionContextEvent();
		assertNullValue("Unexpected initial context", SpringEventReferenceCollectionAspect.getApplicationContext(event));
		assertTrue("Event class not marked without context", SpringEventReferenceCollectionAspect.nonContextEvents.contains(event.getClass()));
		assertNotNull("Missing extraction method mapping", SpringEventReferenceCollectionAspect.contextMethods.get(event.getClass()));
	}

	static class StaticContextMethodEvent extends ApplicationEvent {
		private static final long serialVersionUID = 1L;

		public StaticContextMethodEvent() {
			super(Void.class);
		}
		
		public static ApplicationContext getApplicationContext() {
			return Mockito.mock(ApplicationContext.class);
		}
	}
	
	static class ExceptionContextEvent extends ApplicationEvent {
		private static final long serialVersionUID = 1L;

		public ExceptionContextEvent() {
			super(Void.class);
		}

		public final ApplicationContext getApplicationContext() {
			throw new UnsupportedOperationException("N/A");
		}
	}
}
