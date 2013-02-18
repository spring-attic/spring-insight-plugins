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

package com.springsource.insight.plugin.jmx;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.StringFormatterUtils;


/**
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(JmxOperationCollectionTestSupport.TEST_CONTEXT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JmxSetSingleAttributeOperationCollectionAspectTest extends JmxSingleAttributeOperationTestSupport {
	public JmxSetSingleAttributeOperationCollectionAspectTest() {
		super(JmxPluginRuntimeDescriptor.SET_ACTION);
	}

	@Test
	public void testSetDirectSingleAttributeValue() throws Exception {
		testSetSingleAttributeValue(mbeanServer);
	}

	@Test	// make sure that cflowbelow is activated
	public void testSetDelegatedSingleAttributeValue() throws Exception {
		testSetSingleAttributeValue(new DelegatingMBeanServer(mbeanServer));
	}

	private void testSetSingleAttributeValue(MBeanServer server) throws Exception {
		ObjectName	name=new ObjectName(SpringMBeanComponent.RESOURCE_NAME);
		Object[]	valPairs={
				"StringValue", getClass().getSimpleName()
							 + "#testSetSingleAttributeValue("
							 + server.getClass().getSimpleName()
							 + "@" + System.identityHashCode(server)
							 + ")",
				"NumberValue", Long.valueOf(System.nanoTime())
			};

		for (int	index=0; index < valPairs.length; index += 2) {
			String	attrName=String.valueOf(valPairs[index]);
			Object	attrValue=valPairs[index+1];
			server.setAttribute(name, new Attribute(attrName, attrValue));

			Operation	op=assertAttributeOperation(name, attrName);
			Object		opValue=op.get(JmxPluginRuntimeDescriptor.ATTR_VALUE_PROP);
			assertEquals("Mismatched attribute value for " + name.getCanonicalName() + "[" + attrName + "]",
					 	 StringFormatterUtils.formatObject(attrValue),
					 	 StringFormatterUtils.formatObject(opValue));
			Mockito.reset(spiedOperationCollector);
		}
	}
	@Override
	public JmxSetSingleAttributeOperationCollectionAspect getAspect() {
		return JmxSetSingleAttributeOperationCollectionAspect.aspectOf();
	}

}
