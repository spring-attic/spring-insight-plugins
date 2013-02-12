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

import javax.annotation.PostConstruct;

import org.junit.Test;
import org.springframework.beans.factory.InitializingBean;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.SourceCodeLocation;

public class InitializingBeanOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
    public InitializingBeanOperationCollectionAspectTest () {
        super();
    }

    @Test
    public void initializingBean() {
        MyInitializingBean bean = new MyInitializingBean();
        bean.afterPropertiesSet();
        assertInitMethod("afterPropertiesSet");
    }

    @Test
    public void postConstruct() {
        PostConstructBean bean = new PostConstructBean();
        bean.postConstruct();
        assertInitMethod("postConstruct");
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return InitializingBeanOperationCollectionAspect.aspectOf();
    }

    private Operation assertInitMethod (String methodName) {
        Operation op = getLastEntered();
        assertNotNull("No operation", op);
        assertEquals("Mismatched operation type", SpringCorePluginRuntimeDescriptor.BEAN_LIFECYLE_TYPE, op.getType());

        String	compType=op.get(StereotypedSpringBeanMethodOperationCollectionAspectSupport.COMP_TYPE_ATTR, String.class);
        // make sure not intercepted by one of the stereotyped beans aspects
        assertNull("Unexpected stereotyped bean method collection: " + compType, compType);

        SourceCodeLocation	scl=op.getSourceCodeLocation();
        assertEquals("Mismatched method", methodName, scl.getMethodName());
        return op;
    }

    static class MyInitializingBean implements InitializingBean {
        public void afterPropertiesSet() {
        	// do nothing
        }
    }

    static class PostConstructBean {
        @PostConstruct
        public void postConstruct() {
        	// do nothing
        }
    }
}
