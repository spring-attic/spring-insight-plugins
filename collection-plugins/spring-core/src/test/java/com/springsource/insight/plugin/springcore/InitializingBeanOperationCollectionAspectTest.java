package com.springsource.insight.plugin.springcore;

import static org.junit.Assert.assertEquals;

import javax.annotation.PostConstruct;

import org.junit.Test;
import org.springframework.beans.factory.InitializingBean;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

public class InitializingBeanOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
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
