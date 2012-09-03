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

package com.springsource.insight.plugin.rmi;

import java.rmi.Remote;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.util.ListUtil;

/**
 */
public class RmiListOperationCollectionAspectTest extends RmiOperationCollectionAspectTestSupport {

	public RmiListOperationCollectionAspectTest() {
		super();
	}
	
	@Test
	public void testList() throws Exception {
		List<String> names = Arrays.asList("111", "222", "333", "444");
		Remote rem = Mockito.mock(Remote.class);
		for (String n : names) {
			registry.put(n, rem);
		}
		
		List<String> res = Arrays.asList(registry.list());
		if (! ListUtil.compareCollections(names, res)) {
			fail("Mismatched resutls in array - expected: " + names + ", actual: " + res);
		}
		
		Operation op = getLastEntered();
		assertNotNull("No operation", op);
		assertEquals("Mismatched type", RmiDefinitions.RMI_LIST, op.getType());
		
		OperationList list = op.get(RmiDefinitions.LIST_ATTR, OperationList.class);
		assertNotNull("Missing names list", list);
		assertEquals("Mismatched names list size", res.size(), list.size());
		for (int index = 0; index < list.size(); index++) {
			String expected = res.get(index), actual = list.get(index, String.class);
			assertEquals("Mismatched value @ index " + index, expected, actual);
		}
	}

	@Override
	public OperationCollectionAspectSupport getAspect() {
		return RmiListOperationCollectionAspect.aspectOf();
	}

}
