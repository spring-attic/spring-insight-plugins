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
package com.springsource.insight.plugin.hadoop;

import org.junit.Ignore;
import org.junit.Test;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;


public class JobOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	public JobOperationCollectionAspectTest () {
		super();
	}

	@Override
	public OperationCollectionAspectSupport getAspect() {
		return JobOperationCollectionAspect.aspectOf();
	}
	
	@Test
    @Ignore("Fails on Windows with: Failed to set permissions of path: \\tmp\\hadoop-XXX\\mapred\\stagin\\XXX-327756435\\.staging to 0700")
	public void testRun() throws Exception {
		// Step 1: Execute test
		new WordCount().run(null);

		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();
		assertNotNull("No Hadoop-Reduce operation data is intercepted",op);

		// Step 3:  Validate
		assertEquals("Invalid operation type", OperationCollectionTypes.JOB_TYPE.type, op.getType());
	}
}