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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;

import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;

public abstract class AbstractOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	public void validate(OperationType opType, String... p_params) throws Exception {
		// Step 2:  Get the Operation that was just created by our aspect
		Operation op = getLastEntered();
		Assert.assertNotNull("No operation data is intercepted", op);

		// Step 3:  Validate operation type
		Assert.assertTrue("Invalid operation type: "+op.getType().getName()+", expected: "+opType, op.getType().equals(opType));
		
		// prepare parameters
		List<String> params=new ArrayList<String>();
		Collections.addAll(params, p_params); 
		params.add("server = localhost:9160");
		// Step 4: Validate parameters
		for (String def: params) {
			String[] param=null;
			int indx=def.indexOf("=");
			if (indx>0) {
				param=new String[]{def.substring(0, indx).trim(), def.substring(indx+1).trim()};
			}
			else
				param=new String[]{def};
			
			// get value
			Object value=op.get(param[0]);
			
			// validate parameter's value
			if (param.length==1) {
				Assert.assertNotNull("Parameter ["+param[0]+"] does not exists", value);
			}
			else {
				if (value instanceof String)
					Assert.assertEquals("Invalid ["+param[0]+"] parameter: "+value, param[1], value);
				else
				if (value instanceof OperationMap) {
					for(String mapEntry: param[1].substring(1, param[1].length()-1).split("\\s*,\\s*")) {
						String[] expectedMapEntry=mapEntry.split("\\s*=\\s*");
						
						Object mapValue=((OperationMap)value).get(expectedMapEntry[0]);
						if (expectedMapEntry.length==1) {
							Assert.assertNotNull("Map key ["+expectedMapEntry[0]+"] does not exists", mapValue);
						}
						else {
							Assert.assertEquals("Invalid ["+expectedMapEntry[0]+"] map key value: "+mapValue, expectedMapEntry[1], mapValue);
						}
					}
				}
			}
		}
	} 
}
