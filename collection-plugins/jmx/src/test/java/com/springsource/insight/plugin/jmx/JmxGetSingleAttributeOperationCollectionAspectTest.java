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

import java.util.Map;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
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
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.ExceptionUtils;
import com.springsource.insight.util.MapUtil;
import com.springsource.insight.util.StringFormatterUtils;


/**
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(JmxOperationCollectionTestSupport.TEST_CONTEXT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JmxGetSingleAttributeOperationCollectionAspectTest extends JmxSingleAttributeOperationTestSupport {
	public JmxGetSingleAttributeOperationCollectionAspectTest() {
		super(JmxPluginRuntimeDescriptor.GET_ACTION);
	}

	@Test
	public void testGetDirectAttribute() throws Exception {
		testGetAttribute(mbeanServer);
	}

	@Test	// makes sure that cflowbelow is activated
	public void testGetDelegateAttribute() throws Exception {
		testGetAttribute(new DelegatingMBeanServer(mbeanServer));
	}

	private void testGetAttribute(MBeanServer server) throws Exception {
		Map<ObjectName,MBeanInfo>	beansMap=getBeansMap(server);
		assertFalse("No beans", MapUtil.size(beansMap) <= 0);
		
		int	attrsCount=0;
		for (Map.Entry<ObjectName, MBeanInfo> je : beansMap.entrySet()) {
			ObjectName				name=je.getKey();
			MBeanInfo				info=je.getValue();
			MBeanAttributeInfo[]	attrs=info.getAttributes();
			if (ArrayUtil.length(attrs) <= 0) {
				_logger.info("Skip " + name.getCanonicalName() + " - no attributes");
				continue;
			}
			
			_logger.info("Check attributes of " + name.getCanonicalName());
			for (MBeanAttributeInfo attrInfo : attrs) {
				String	attrName=attrInfo.getName();
				if (!attrInfo.isReadable()) {
					_logger.info("getAttribute(" + name.getCanonicalName() + "[" + attrName + "] skip - non-readable");
					continue;
				}

				final Object	attrValue;
				try {
					try {
						attrValue = server.getAttribute(name, attrName);
						_logger.info("getAttribute(" + name.getCanonicalName() + "[" + attrName + "]: " + StringFormatterUtils.formatObject(attrValue));
						attrsCount++;
					} catch(Exception e) {
						Throwable	t=ExceptionUtils.peelThrowable(e);
						_logger.warning("getAttribute(" + name.getCanonicalName() + "[" + attrName + "]"
									  + " failed (" + t.getClass().getSimpleName() + ")"
									  + " to retrieve: " + t.getMessage());
						continue;
					}
	
					Operation	op=assertAttributeOperation(name, attrName);
					Object		retValue=op.get(OperationFields.RETURN_VALUE);
					if (attrValue != null) {
						assertEquals("Mismatched attribute value for " + name.getCanonicalName() + "[" + attrName + "]",
									 StringFormatterUtils.formatObject(attrValue),
									 StringFormatterUtils.formatObject(retValue));
					}
				} finally {
					Mockito.reset(spiedOperationCollector);
				}
			}
		}
		
		assertTrue("No attributes tested", attrsCount > 0);
	}

	@Override
	public JmxGetSingleAttributeOperationCollectionAspect getAspect() {
		return JmxGetSingleAttributeOperationCollectionAspect.aspectOf();
	}
}
