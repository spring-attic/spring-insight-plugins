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

import static org.junit.Assert.assertEquals;

import javax.annotation.PostConstruct;

import org.junit.Test;
import org.springframework.beans.factory.InitializingBean;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

public class InitializingBeanOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
    public InitializingBeanOperationCollectionAspectTest () {
        super();
    }

    @Test
    public void initializingBean() {
        MyInitializingBean bean = new MyInitializingBean();
        bean.afterPropertiesSet();
        Operation op = getLastEntered();
        assertEquals("afterPropertiesSet", op.getSourceCodeLocation().getMethodName());
    }

    @Test
    public void postConstruct() {
        PostConstructBean bean = new PostConstructBean();
        bean.postConstruct();
        Operation op = getLastEntered();
        assertEquals("postConstruct", op.getSourceCodeLocation().getMethodName());
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return InitializingBeanOperationCollectionAspect.aspectOf();
    }

    class MyInitializingBean implements InitializingBean {
        public void afterPropertiesSet() {

        }
    }

    class PostConstructBean {
        @PostConstruct
        public void postConstruct() {

        }
    }
}
