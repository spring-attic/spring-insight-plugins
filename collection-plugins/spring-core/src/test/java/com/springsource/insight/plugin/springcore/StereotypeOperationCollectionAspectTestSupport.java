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

package com.springsource.insight.plugin.springcore;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.method.AnnotationDrivenMethodOperationCollectionAspect;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

/**
 * 
 */
public abstract class StereotypeOperationCollectionAspectTestSupport
		extends	OperationCollectionAspectTestSupport {

	public StereotypeOperationCollectionAspectTestSupport() {
		super();
	}

	protected Operation assertStereotypeOperation (Class<?> beanClass, String beanMethod) {
        Operation   operation=getLastEntered();
        assertEquals("Mismatched label", beanClass.getSimpleName() + "#" + beanMethod, operation.getLabel());
        return operation;
	}

	@Override
	public OperationCollectionAspectSupport getAspect() {
        return AnnotationDrivenMethodOperationCollectionAspect.aspectOf();
	}
}
