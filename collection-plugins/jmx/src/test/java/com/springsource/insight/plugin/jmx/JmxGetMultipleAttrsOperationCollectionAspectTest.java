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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.management.AttributeList;
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
import com.springsource.insight.util.ExceptionUtils;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.MapUtil;


/**
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(JmxOperationCollectionTestSupport.TEST_CONTEXT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JmxGetMultipleAttrsOperationCollectionAspectTest extends JmxMultiAttributeCollectionTestSupport {
	public JmxGetMultipleAttrsOperationCollectionAspectTest() {
		super(JmxPluginRuntimeDescriptor.GET_ACTION);
	}

	@Test
	public void testGetDirectAttributesList() throws Exception {
		testGetMultipleAttributes(mbeanServer);
	}

	@Test	// make sure cflowbelow activated
	public void testGetDelegatedAttributesList() throws Exception {
		testGetMultipleAttributes(new DelegatingMBeanServer(mbeanServer));
	}

	private void testGetMultipleAttributes(MBeanServer server) throws Exception {
		Map<ObjectName,MBeanInfo>	beansMap=getBeansMap(server);
		assertFalse("No beans", MapUtil.size(beansMap) <= 0);
		
		int	beansCount=0;
		for (Map.Entry<ObjectName, MBeanInfo> je : beansMap.entrySet()) {
			ObjectName		name=je.getKey();
			MBeanInfo		info=je.getValue();
			List<String>	attrsNames=getReadableAttributes(info);
			if (ListUtil.size(attrsNames) <= 0) {
				_logger.info("Skip " + name.getCanonicalName() + " - no readable attributes");
				continue;
			}
			
			for (int index=0; index < Byte.SIZE; index++) {
				Collections.shuffle(attrsNames);

				String[]	attributes=attrsNames.toArray(new String[attrsNames.size()]);
				try {
					final AttributeList	values;
					try {
						values = server.getAttributes(name, attributes);
						_logger.info("getAttributes(" + name.getCanonicalName() + ")"
								   + Arrays.toString(attributes)
								   + " - count=" + ListUtil.size(values));
						if (index == 0) {
							beansCount++;
						}
					} catch(Exception e) {
						Throwable	t=ExceptionUtils.peelThrowable(e);
						_logger.warning("Failed (" + t.getClass().getSimpleName() + "]"
									  + " to retrieve " + name.getCanonicalName()
									  + " attributes=" + Arrays.toString(attributes)
									  + ": " + t.getMessage());
						break;
					}
					
					Operation	op=assertAttributesListOperation(name, attributes);
					assertEncodedManagedAttributes(op, name, values);
				} finally {
					Mockito.reset(spiedOperationCollector);
				}
			}
		}
		
		assertTrue("No beans accessed", beansCount > 0);
	}

	@Override
	public JmxGetMultipleAttrsOperationCollectionAspect getAspect() {
		return JmxGetMultipleAttrsOperationCollectionAspect.aspectOf();
	}
}
