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

package com.springsource.insight.plugin.jndi;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;

import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mockito;

import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.util.MapUtil;

/**
 * 
 */
public abstract class JndiOperationCollectionAspectTestSupport
			extends OperationCollectionAspectTestSupport {

    protected final OperationType	type;
    protected final JndiEndpointAnalyzer	analyzer=new JndiEndpointAnalyzer();
    private final JndiTestContext	testContext=new JndiTestContext();

	protected JndiOperationCollectionAspectTestSupport(OperationType opType) {
		if ((type=opType) == null) {
			throw new IllegalStateException("No operation type specified");
		}
	}

	@Override
	@Before
	public void setUp() {
		super.setUp();
		testContext.clear();
	}

	protected EndPointAnalysis assertEndPointAnalysis (Operation op) {
		Frame	frame=Mockito.mock(Frame.class);
		Mockito.when(frame.getOperation()).thenReturn(op);

		EndPointAnalysis	analysis=analyzer.locateEndPoint(frame, 0);
		Assert.assertSame("Mismatched source operation for " + op.getLabel(), op, analysis.getSourceOperation());
		Assert.assertEquals("Mismatched endpoint for " + op.getLabel(), EndPointName.valueOf(op), analysis.getEndPointName());
		Assert.assertNotNull("No analysis for " + op.getLabel(), analysis);
		Assert.assertEquals("Mismatched score for " + op.getLabel(), JndiEndpointAnalyzer.DEFAULT_SCORE, analysis.getScore());
		return analysis;
	}

	protected Operation assertCollectedOperation (String action, String name) {
		Operation	op=getLastEntered();
		Assert.assertNotNull("No operation created", op);
		Assert.assertEquals("Mismatched type", type, op.getType());
		Assert.assertEquals("Mismatched action", action, op.get("action", String.class));
		Assert.assertEquals("Mismatched name", name, op.get("name", String.class));
		return op;
	}

	protected OperationMap assertCollectedEnvironment (Operation op, Context context) throws NamingException {
		return assertCollectedEnvironment(op, context.getEnvironment());
	}

	protected OperationMap assertCollectedEnvironment (Operation op, Map<?,?> env) {
		OperationMap	envMap=op.get("environment", OperationMap.class);
		Assert.assertNotNull("Missing environment in " + op.getLabel(), envMap);
		Assert.assertEquals("Mismatched environment size in " + op.getLabel(), MapUtil.size(env), envMap.size());

		Collection<? extends Map.Entry<String,?>>	envEntries=envMap.entrySet();
		Collection<Object>							expKeys=new HashSet<Object>(env.keySet());
		for (Map.Entry<String,?> ee : envEntries) {
			String	key=ee.getKey();
			Object	expected=env.get(key), actual=ee.getValue();
			Assert.assertEquals(op.getLabel() + ": Mismatched values for key=" + key, expected, actual);
			Assert.assertTrue(op.getLabel() + ": Unmatched key: " + key, expKeys.remove(key));
		}

		Assert.assertTrue("Not all keys exhaused for " + op.getLabel() + ": " + expKeys, expKeys.isEmpty());
		return envMap;
	}

	protected JndiTestContext setUpContext (Map<String,?> bindings, Map<String,?> env) {
		testContext.setBindings(bindings);
		testContext.setEnvironment(env);
		return testContext;
	}
}
