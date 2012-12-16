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

package com.springsource.insight.plugin.springcore;

import java.lang.annotation.Annotation;
import java.util.List;

import org.springframework.context.ApplicationEvent;

import com.foo.example.AbstractBean;
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollector;
import com.springsource.insight.collection.OperationListCollector;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.util.ListUtil;

/**
 * 
 */
public abstract class StereotypeOperationCollectionAspectTestSupport
		extends	OperationCollectionAspectTestSupport {

	protected final Class<? extends Annotation>	stereoTypeClass;

	protected StereotypeOperationCollectionAspectTestSupport(Class<? extends Annotation> annClass) {
		if ((stereoTypeClass=annClass) == null) {
			throw new IllegalStateException("No stereotype class provided");
		}
	}

    @Override
	protected OperationCollector createSpiedOperationCollector(OperationCollector originalCollector) {
        assertNotNull("No original collector", originalCollector);
		return new OperationListCollector();
	}

	@Override
	protected Operation getLastEnteredOperation(OperationCollector spiedCollector) {
        List<Operation>	opsList=((OperationListCollector) spiedCollector).getCollectedOperations();
		if (ListUtil.size(opsList) <= 0) {
			return null;
		} else {
			return opsList.get(opsList.size() - 1);
		}
	}

	protected void assertLifecycleMethodsNotIntercepted(AbstractBean beanInstance) throws Exception {
		OperationCollectionAspectSupport	aspectInstance=getAspect();
		OperationCollector					orgCollector=aspectInstance.getCollector();
		OperationListCollector				collector=new OperationListCollector();
		aspectInstance.setCollector(collector);
		
		List<Operation>	collectedOps=collector.getCollectedOperations();
		try {
			beanInstance.afterPropertiesSet();
			assertTrue("Unexpected invocation for 'afterPropertiesSet': " + collectedOps, collectedOps.isEmpty());

			ApplicationEvent	testEvent=new TestEvent(beanInstance);

			beanInstance.onApplicationEvent(testEvent);
			assertTrue("Unexpected invocation for 'onApplicationEvent': " + collectedOps, collectedOps.isEmpty());

			beanInstance.publishEvent(testEvent);
			assertTrue("Unexpected invocation for 'publishEvent': " + collectedOps, collectedOps.isEmpty());

			beanInstance.multicastEvent(testEvent);
			assertTrue("Unexpected invocation for 'multicastEvent': " + collectedOps, collectedOps.isEmpty());
		} finally {
			aspectInstance.setCollector(orgCollector);
		}
	}

	protected Operation assertStereotypeOperation (Runnable beanInstance, boolean withOperation) {
		Class<?>	beanClass=beanInstance.getClass();
		Annotation	ann=beanClass.getAnnotation(stereoTypeClass);
		assertNotNull("Missing stereotype @" + stereoTypeClass.getSimpleName(), ann);

		beanInstance.run();

		Operation   operation=getLastEntered();
		if (withOperation) {
			assertStereotypeOperation(operation, beanClass, "run");
		} else if (operation != null) {
        	fail(beanClass.getSimpleName() + " unexpected operation: " + operation.getLabel());
        }
		return operation;
	}

	protected Operation assertStereotypeOperation (Operation operation, Class<?> beanClass, String beanMethod) {
        assertNotNull(beanClass.getSimpleName() + " no operation", operation);
        assertEquals(beanClass.getSimpleName() + " mismatched type", OperationType.METHOD, operation.getType());
        assertEquals("Mismatched component type", stereoTypeClass.getSimpleName(), operation.get(StereotypedSpringBeanMethodOperationCollectionAspectSupport.COMP_TYPE_ATTR, String.class));
        assertEquals("Mismatched label", beanClass.getSimpleName() + "#" + beanMethod, operation.getLabel());
        return operation;
	}
	
	protected static class TestEvent extends ApplicationEvent {
		private static final long serialVersionUID = 1L;

		public TestEvent(AbstractBean beanInstance) {
			super(beanInstance);
		}
	}
}
