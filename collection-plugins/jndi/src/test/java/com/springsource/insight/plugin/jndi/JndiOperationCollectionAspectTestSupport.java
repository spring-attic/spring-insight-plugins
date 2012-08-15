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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;

import org.junit.Before;
import org.mockito.Mockito;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.util.ListUtil;
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
		assertSame("Mismatched source operation for " + op.getLabel(), op, analysis.getSourceOperation());
		assertEquals("Mismatched endpoint for " + op.getLabel(), EndPointName.valueOf(op), analysis.getEndPointName());
		assertNotNull("No analysis for " + op.getLabel(), analysis);
		assertEquals("Mismatched score for " + op.getLabel(), JndiEndpointAnalyzer.DEFAULT_SCORE, analysis.getScore());
		return analysis;
	}

	protected Operation assertCollectedOperation (String action, String name) {
		Operation	op=getLastEntered();
		assertNotNull("No operation created", op);
		assertEquals("Mismatched type", type, op.getType());
		assertEquals("Mismatched action", action, op.get("action", String.class));
		assertEquals("Mismatched name", name, op.get("name", String.class));
		return op;
	}

	protected OperationMap assertCollectedEnvironment (Operation op, Context context) throws NamingException {
		return assertCollectedEnvironment(op, context.getEnvironment());
	}

	protected OperationMap assertCollectedEnvironment (Operation op, Map<?,?> env) {
		OperationMap	envMap=op.get("environment", OperationMap.class);
		assertNotNull("Missing environment in " + op.getLabel(), envMap);
		assertEquals("Mismatched environment size in " + op.getLabel(), MapUtil.size(env), envMap.size());

		Collection<? extends Map.Entry<String,?>>	envEntries=envMap.entrySet();
		Collection<Object>							expKeys=new HashSet<Object>(env.keySet());
		for (Map.Entry<String,?> ee : envEntries) {
			String	key=ee.getKey();
			Object	expected=env.get(key), actual=ee.getValue();
			assertEquals(op.getLabel() + ": Mismatched values for key=" + key, expected, actual);
			assertTrue(op.getLabel() + ": Unmatched key: " + key, expKeys.remove(key));
		}

		assertTrue("Not all keys exhaused for " + op.getLabel() + ": " + expKeys, expKeys.isEmpty());
		return envMap;
	}

	protected JndiTestContext setUpContext (Map<String,?> bindings, Map<String,?> env) {
		testContext.setBindings(bindings);
		testContext.setEnvironment(env);
		return testContext;
	}

	protected void runFilteredResourcesTest (String baseName, ContextOperationExecutor executor) throws Exception {
		runFilteredResourcesTest(baseName, setUpContext(Collections.<String,Object>emptyMap(), Collections.<String,Object>emptyMap()), executor);
	}

	protected void runFilteredResourcesTest (
			String baseName, JndiTestContext context, ContextOperationExecutor executor)
				throws Exception {
		OperationCollectionAspectSupport	aspectInstance=getAspect();
		OperationCollector					collector=aspectInstance.getCollector();
		try {
			OperationListCollector	testCollector=new OperationListCollector();
			aspectInstance.setCollector(testCollector);

			for (String suffix : JndiResourceCollectionFilter.DEFAULT_EXCLUSION_PATTERNS) {
				String	name=baseName + "." + suffix;
				executor.executeContextOperation(context, name, suffix);

				Collection<Operation>	opsList=testCollector.getCollectedOperations();
				assertEquals(baseName + "[" + suffix + "] unexpected operations: " + opsList, 0, ListUtil.size(opsList));
			}
		} finally {
			aspectInstance.setCollector(collector);
		}
	}

	protected static interface ContextOperationExecutor {
		Object executeContextOperation (JndiTestContext context, String name, Object value) throws Exception;
	}
}
