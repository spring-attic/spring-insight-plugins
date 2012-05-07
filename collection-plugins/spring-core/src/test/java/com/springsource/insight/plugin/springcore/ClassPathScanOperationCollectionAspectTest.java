package com.springsource.insight.plugin.springcore;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Set;

import static com.springsource.insight.util.ListUtil.asSet;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

public class ClassPathScanOperationCollectionAspectTest  extends OperationCollectionAspectTestSupport {
    @Test
    public void methodsIntercepted() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("com/springsource/insight/plugin/springcore/test-class-path-scan-operation.xml");
        assertNotNull(ctx.getBean(Fubar.class));

        ArgumentCaptor<Operation> opCaptor = ArgumentCaptor.forClass(Operation.class);
        verify(spiedOperationCollector, atLeastOnce()).enter(opCaptor.capture());

        Set<String> methodsToFind = asSet("refresh", "findCandidateComponents", "findPathMatchingResources");
        for (Operation op : opCaptor.getAllValues()) {
            methodsToFind.remove(op.getSourceCodeLocation().getMethodName());
        }

        assertTrue("Aspect did not intercept call to " + methodsToFind, methodsToFind.isEmpty());
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return ClassPathScanOperationCollectionAspect.aspectOf();
    }
}
