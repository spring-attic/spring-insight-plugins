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

package com.springsource.insight.plugin.jndi;

import java.util.Collections;
import java.util.Map;

import javax.naming.NamingException;

import org.junit.Test;

import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public class JndiLookupOperationCollectionAspectTest extends JndiOperationCollectionAspectTestSupport {
	public JndiLookupOperationCollectionAspectTest() {
		super(JndiEndpointAnalyzer.LOOKUP);
	}

	@Test
	public void testLookup () throws Exception {
		final String	NAME="testLookup";
		JndiTestContext	context=setUpContext(Collections.singletonMap(NAME, Long.valueOf(System.currentTimeMillis())),
											 Collections.singletonMap(NAME, Long.valueOf(System.nanoTime())));
		Object	value=context.lookup(NAME);
		assertLookupOperation(context, "lookup", NAME, value);
	}

	@Test
	public void testLookupLink () throws Exception {
		final String	NAME="testLookupLink";
		JndiTestContext	context=setUpContext(Collections.singletonMap(NAME, Long.valueOf(System.currentTimeMillis())),
											 Collections.singletonMap(NAME, Long.valueOf(System.nanoTime())));
		Object	value=context.lookupLink(NAME);
		assertLookupOperation(context, "lookupLink", NAME, value);
	}

	@Test
	public void testIgnoredResourcesLookup () throws Exception {
		runFilteredResourcesTest("testIgnoredResourcesLookup",
				new ContextOperationExecutor() {
					public Object executeContextOperation(JndiTestContext context, String name, Object value) throws Exception {
						return context.lookup(name);
					}
				});
	}

	@Test
	public void testIgnoredResourcesLookupLink () throws Exception {
		runFilteredResourcesTest("testIgnoredResourcesLookup",
				new ContextOperationExecutor() {
					public Object executeContextOperation(JndiTestContext context, String name, Object value) throws Exception {
						return context.lookupLink(name);
					}
				});
	}

	protected Operation assertLookupOperation (JndiTestContext context, String action, String name, Object actual) throws NamingException {
		Operation		op=assertCollectedOperation(action, name);
		Map<String,?>	values=context.getBindings();
		Object			expected=values.get(name);
		assertEquals("Mismatched value for " + action + "[" + name + "]", expected, actual);
		assertCollectedEnvironment(op, context);
		assertEndPointAnalysis(op);
		return op;
	}

	@Override
	public JndiLookupOperationCollectionAspect getAspect() {
		return JndiLookupOperationCollectionAspect.aspectOf();
	}

}
