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
package com.springsource.insight.plugin.neo4j;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/test-context.xml"})
@Transactional
public class LookupOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	@Autowired
	OperationCollectionAspectTests tests;
	
	
	public LookupOperationCollectionAspectTest () {
		super();
	}

	@Override
	public OperationCollectionAspectSupport getAspect() {
		return LookupOperationCollectionAspect.aspectOf();
	}
	
	@Test
	public void testRun1() throws Exception {
		// Step 1: Execute test
		tests.test_Lookup1();

		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();
		assertNotNull("No Neo4J.Lookup operation data is intercepted",op);

		// Step 3:  Validate
		assertEquals("Invalid operation type", OperationCollectionTypes.LOOKUP_TYPE.type, op.getType());
		
		assertEquals("Invalid Label", OperationCollectionTypes.LOOKUP_TYPE.label, op.getLabel());
		
		assertNotNull("Parameter 'indexName' does not exists", op.get("indexName"));
		assertNotNull("Parameter 'query' does not exists", op.get("query"));
	}
	
	@Test
	public void testRun2() throws Exception {
		// Step 1: Execute test
		tests.test_Lookup2();

		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();
		assertNotNull("No Neo4J.Lookup operation data is intercepted",op);

		// Step 3:  Validate
		assertEquals("Invalid operation type", OperationCollectionTypes.LOOKUP_TYPE.type, op.getType());
		
		assertEquals("Invalid Label", OperationCollectionTypes.LOOKUP_TYPE.label, op.getLabel());
		
		assertNotNull("Parameter 'indexName' does not exists", op.get("indexName"));
		assertNotNull("Parameter 'field' does not exists", op.get("field"));
		assertNotNull("Parameter 'value' does not exists", op.get("value"));
	}
	
	@Test
	public void testRun3() throws Exception {
		// Step 1: Execute test
		tests.test_Lookup3();

		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();
		assertNotNull("No Neo4J.Lookup operation data is intercepted",op);

		// Step 3:  Validate
		assertEquals("Invalid operation type", OperationCollectionTypes.LOOKUP_TYPE.type, op.getType());
		
		assertEquals("Invalid Label", OperationCollectionTypes.LOOKUP_TYPE.label, op.getLabel());
		
		assertNotNull("Parameter 'indexedType' does not exists", op.get("indexedType"));
		assertNotNull("Parameter 'propertyName' does not exists", op.get("propertyName"));
		assertNotNull("Parameter 'value' does not exists", op.get("value"));
	}
}