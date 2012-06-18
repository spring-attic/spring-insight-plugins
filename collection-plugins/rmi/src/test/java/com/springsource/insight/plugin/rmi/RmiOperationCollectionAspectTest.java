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

package com.springsource.insight.plugin.rmi;

import java.rmi.Remote;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.intercept.operation.Operation;

/**
 */
public class RmiOperationCollectionAspectTest extends RmiOperationCollectionAspectTestSupport {
	public RmiOperationCollectionAspectTest() {
		super();
	}

	@Test
	public void testLookup() throws Exception {
		String name = "testLookup";
		Remote rem = Mockito.mock(Remote.class);
		registry.put(name, rem);
		Remote res = registry.lookup(name);
		Assert.assertSame("Mismatched results", rem , res);
		assertRmiOperation("lookup", name);
	}
	
	@Test
	public void testUnbind() throws Exception {
		String name = "testUnbind";
		Remote rem = Mockito.mock(Remote.class);
		registry.put(name, rem);
		registry.unbind(name);
		
		assertRmiOperation("unbind", name);
	}
	
	@Test
	public void testBind() throws Exception {
		String name = "testBind";
		Remote rem = Mockito.mock(Remote.class);
		registry.bind(name, rem);
		
		assertRmiOperation("bind", name);
	}
	
	@Test
	public void testRebind() throws Exception {
		String name = "testRebind";
		Remote rem = Mockito.mock(Remote.class);
		registry.rebind(name, rem);
		
		assertRmiOperation("rebind", name);
	}
	
	private Operation assertRmiOperation (String action, String name) {
		Operation	op=getLastEntered();
		Assert.assertNotNull("No operation extracted", op);
		Assert.assertEquals("Mismatched action", action, op.get(RmiDefinitions.ACTION_ATTR, String.class));
		Assert.assertEquals("Mismatched name", name, op.get(RmiDefinitions.NAME_ATTR, String.class));
		return op;
	}

	@Override
	public OperationCollectionAspectSupport getAspect() {
		return RmiOperationCollectionAspect.aspectOf();
	}

}
