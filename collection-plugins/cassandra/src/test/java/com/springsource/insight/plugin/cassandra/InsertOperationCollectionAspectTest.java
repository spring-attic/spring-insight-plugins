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
package com.springsource.insight.plugin.cassandra;

import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;

public class InsertOperationCollectionAspectTest extends AbstractOperationCollectionAspectTest{
	public OperationCollectionAspectSupport getAspect() {
		return UpdateOperationCollectionAspect.aspectOf();
	}
	
	@Test
	public void test1() throws Exception {
		CassandraUnitTests.getInstance().testInsert();
		validate(OperationCollectionTypes.UPDATE_TYPE.type,
				"columnFamily=Standard1",
				"key=1","colName=name","colValue","colTimestamp","consistLevel",
				"returnValue");
	}
}
